// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

import org.gradle.crypto.checksum.Checksum
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.System.getenv
import java.net.URI

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.gradle.crypto.checksum") version "1.4.0" apply false
    id("org.cyclonedx.bom") version "1.8.2" apply false
}

group = "ai.ancf.lmos"

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "org.cyclonedx.bom")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.gradle.crypto.checksum")

    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven { setUrl("https://repo.spring.io/milestone") }
        maven { setUrl("https://repo.spring.io/snapshot") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            freeCompilerArgs += "-Xcontext-receivers"
            jvmTarget = "17"
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        // withJavadocJar()
    }

    dependencies {
        testImplementation(kotlin("test"))
        testImplementation("io.mockk:mockk:1.13.12")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<Test> {
        val runFlowTests = project.findProperty("runFlowTests")?.toString()?.toBoolean() ?: false

        if (!runFlowTests) {
            exclude("**/*Flow*")
        }
    }

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    configure<PublishingExtension> {
        publications {
            create("Maven", MavenPublication::class.java) {
                from(components["java"])
                artifact(javadocJar)
                pom {
                    if (project.isBOM()) packaging = "pom"
                    description = "Efficient Agent Routing with SOTA Language and Embedding Models."
                    url = "https://github.com/lmos-ai/lmos-router"
                    scm {
                        url = "https://github.com/lmos-ai/lmos-router.git"
                    }
                    licenses {
                        license {
                            name = "Apache-2.0"
                            distribution = "repo"
                            url = "https://github.com/lmos-ai/lmos-router/blob/main/LICENSES/Apache-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "xmxnt"
                            name = "Amant Kumar"
                            email = "opensource@telekom.de"
                        }
                        developer {
                            id = "jas34"
                            name = "Jasbir Singh"
                            email = "opensource@telekom.de"
                        }
                        developer {
                            id = "merrenfx"
                            name = "Max Erren"
                            email = "opensource@telekom.de"
                        }
                    }
                }
            }

            group = "ai.ancf.lmos"
            version = rootProject.version
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = URI("https://maven.pkg.github.com/lmos-ai/lmos-router")
                    credentials {
                        username = findProperty("GITHUB_USER")?.toString() ?: getenv("GITHUB_USER")
                        password = findProperty("GITHUB_TOKEN")?.toString() ?: getenv("GITHUB_TOKEN")
                    }
                }
                maven {
                    name = "OSSRH"
                    url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = getenv("OSSRH_USER")
                        password = getenv("OSSRH_TOKEN")
                    }
                }
            }

            configure<SigningExtension> {
                useInMemoryPgpKeys(
                    findProperty("signing.keyId") as String?,
                    getenv("PGP_SECRET_KEY"),
                    getenv("PGP_PASSPHRASE"),
                )
                sign(publications)
            }
        }
    }

    tasks.register<Jar>("sourceJar") {
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }

    tasks.register("copyPom") {
        doLast {
            println("${findProperty("LOCAL_MAVEN_REPO")}/ai/ancf/lmos/${project.name}/${project.version}")
            val pomFolder =
                File("${findProperty("LOCAL_MAVEN_REPO")}/ai/ancf/lmos/${project.name}/${project.version}")
            pomFolder.listFiles()?.forEach { file ->
                if (file.name.endsWith(".pom") || file.name.endsWith(".pom.asc")) {
                    file.copyTo(
                        File(project.layout.buildDirectory.dir("libs").get().asFile, file.name),
                        overwrite = true,
                    )
                }
            }
        }
    }

    tasks.register("cleanChecksum") {
        dependsOn("copyPom")
        doFirst {
            layout.buildDirectory.dir("libs").get().asFile.walk().forEach { file ->
                if (file.name.endsWith(".sha1") || file.name.endsWith(".md5")) {
                    println("Deleting ${file.name} ${file.delete()}")
                }
            }
        }
    }

    tasks.register<Checksum>("checksum") {
        dependsOn("cleanChecksum")
        inputFiles.setFrom(project.layout.buildDirectory.dir("libs"))
        outputDirectory.set(project.layout.buildDirectory.dir("libs"))
        checksumAlgorithm.set(Checksum.Algorithm.MD5)
    }

    tasks.register("sha1") {
        dependsOn("checksum")
        doLast {
            project.layout.buildDirectory.dir("libs").get().asFile.listFiles()?.forEach { file ->
                if (!file.name.endsWith(".md5")) {
                    "shasum ${file.name}".execWithCode(workingDir = file.parentFile).second.forEach {
                        File(file.parentFile, "${file.name}.sha1").writeText(it.substringBefore(" "))
                    }
                }
            }
        }
    }

    tasks.register("setupFolders") {
        dependsOn("sha1")
        doLast {
            val build =
                File(
                    project.layout.buildDirectory.dir("out").get().asFile,
                    "/ai/ancf/lmos/${project.name}/${project.version}",
                )
            build.mkdirs()
            project.layout.buildDirectory.dir("libs").get().asFile.listFiles()?.forEach { file ->
                file.copyTo(File(build, file.name), overwrite = true)
            }
        }
    }

    tasks.register<Zip>("packageSonatype") {
        doFirst {
            if (project.isBOM()) {
                println("Packaging BOM")
                project.layout.buildDirectory.dir("out").get().asFile.walk().forEach { file ->
                    if (file.isFile && !file.name.contains(".pom")) {
                        println("Deleting ${file.name}")
                        file.delete()
                    }
                }
            }
        }
        dependsOn("setupFolders")
        archiveFileName.set("${project.name}-${project.version}.zip")
        destinationDirectory.set(parent!!.layout.buildDirectory.dir("dist"))
        from(layout.buildDirectory.dir("out"))
    }

    // WIP
    tasks.register<Exec>("uploadSonatype") {
        group = "sonatype"
        dependsOn("packageSonatype")
        workingDir = project.rootDir
        commandLine(
            "./upload.sh",
            "build/dist/${project.name}-${project.version}.zip",
            findProperty("SONATYPE_TOKEN"),
        )
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

fun Project.java(configure: Action<JavaPluginExtension>): Unit = (this as ExtensionAware).extensions.configure("java", configure)

fun String.execWithCode(workingDir: File? = null): Pair<CommandResult, Sequence<String>> {
    ProcessBuilder().apply {
        workingDir?.let { directory(it) }
        command(split(" "))
        redirectErrorStream(true)
        val process = start()
        val result = process.readStream()
        val code = process.waitFor()
        return CommandResult(code) to result
    }
}

private fun Process.readStream() =
    sequence<String> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) {
                    break
                }
                yield(line)
            }
        } finally {
            reader.close()
        }
    }

class CommandResult(code: Int) {
    val isFailed = code != 0
    val isSuccess = !isFailed

    fun ifFailed(block: () -> Unit) {
        if (isFailed) block()
    }
}

fun Project.isBOM() = name.endsWith("-bom")
