// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    api(project(":lmos-router-core"))
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    implementation("dev.langchain4j:langchain4j-open-ai:0.36.2")
    implementation("dev.langchain4j:langchain4j-anthropic:0.36.2")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:0.36.2")
    implementation("dev.langchain4j:langchain4j-ollama:0.36.2")
}
