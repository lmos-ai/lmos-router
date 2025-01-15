// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

val springBootVersion: String by rootProject.extra

dependencies {
    api(project(":lmos-router-hybrid"))
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.ai:spring-ai-core:1.0.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    testImplementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-SNAPSHOT")
    testImplementation("org.springframework.ai:spring-ai-qdrant-store-spring-boot-starter:1.0.0-SNAPSHOT")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testImplementation("org.testcontainers:qdrant:1.20.1")
}
