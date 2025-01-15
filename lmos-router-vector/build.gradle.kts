// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    testImplementation("org.testcontainers:ollama:1.20.3")
}
