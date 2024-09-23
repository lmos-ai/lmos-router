// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    api(project(":lmos-router-llm"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.1")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.5")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway:4.1.5")
}
