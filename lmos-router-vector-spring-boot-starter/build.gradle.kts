// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    api(project(":lmos-router-vector"))
    implementation("org.springframework.boot:spring-boot-autoconfigure:3.2.5")
    implementation("org.springframework.boot:spring-boot-configuration-processor:3.2.5")
    implementation("org.springframework.ai:spring-ai-core:1.0.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    testImplementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-SNAPSHOT")
    testImplementation("org.springframework.ai:spring-ai-qdrant-store-spring-boot-starter:1.0.0-SNAPSHOT")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.5")
    testImplementation("org.springframework.boot:spring-boot-starter-web:3.2.5")
    testImplementation("org.testcontainers:qdrant:1.20.1")
}
