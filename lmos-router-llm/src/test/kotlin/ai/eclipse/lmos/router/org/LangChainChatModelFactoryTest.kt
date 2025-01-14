// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm.org.eclipse.lmos.router.llm

import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import io.mockk.every
import io.mockk.mockk
import org.eclipse.lmos.router.llm.LangChainChatModelFactory
import org.eclipse.lmos.router.llm.LangChainClientProvider
import org.eclipse.lmos.router.llm.ModelClientProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for [LangChainChatModelFactory].
 */
class LangChainChatModelFactoryTest {
    @Test
    fun `createClient should return OpenAiChatModel for OPENAI provider`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns LangChainClientProvider.OPENAI.name.lowercase()
        every { properties.baseUrl } returns "https://api.openai.com"
        every { properties.apiKey } returns "openai-api-key"
        every { properties.model } returns "gpt-4o-mini"
        every { properties.maxTokens } returns 1000
        every { properties.temperature } returns 0.7
        every { properties.topP } returns 0.9
        every { properties.topK } returns 50
        every { properties.format } returns "json_object"

        // Act
        val client = LangChainChatModelFactory.createClient(properties)

        // Assert
        assertTrue(client is OpenAiChatModel)
    }

    @Test
    fun `createClient should return AnthropicChatModel for ANTHROPIC provider`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns LangChainClientProvider.ANTHROPIC.name.lowercase()
        every { properties.baseUrl } returns "https://api.anthropic.com"
        every { properties.apiKey } returns "anthropic-api-key"
        every { properties.model } returns "claude-v1"
        every { properties.maxTokens } returns 1500
        every { properties.temperature } returns 0.6
        every { properties.topP } returns 0.8
        every { properties.topK } returns 40
        every { properties.format } returns "text"

        // Act
        val client = LangChainChatModelFactory.createClient(properties)

        // Assert
        assertTrue(client is AnthropicChatModel)
    }

    @Test
    fun `createClient should return GoogleAiGeminiChatModel for GEMINI provider`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns LangChainClientProvider.GEMINI.name.lowercase()
        every { properties.baseUrl } returns "" // Gemini model does not use baseUrl
        every { properties.apiKey } returns "gemini-api-key"
        every { properties.model } returns "gemini-1"
        every { properties.maxTokens } returns 2000
        every { properties.temperature } returns 0.5
        every { properties.topP } returns 0.85
        every { properties.topK } returns 30
        every { properties.format } returns "JSON"

        // Act
        val client = LangChainChatModelFactory.createClient(properties)

        // Assert
        assertTrue(client is GoogleAiGeminiChatModel)
    }

    @Test
    fun `createClient should return OllamaChatModel for OLLAMA provider`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns LangChainClientProvider.OLLAMA.name.lowercase()
        every { properties.baseUrl } returns "http://localhost:11434"
        every { properties.apiKey } returns "" // Ollama model does not require API key
        every { properties.model } returns "ollama-model"
        every { properties.maxTokens } returns 0 // Ollama model does not use maxTokens
        every { properties.temperature } returns 0.4
        every { properties.topP } returns 0.0 // Ollama model does not use topP
        every { properties.topK } returns 0 // Ollama model does not use topK
        every { properties.format } returns "" // Ollama model does not use format

        // Act
        val client = LangChainChatModelFactory.createClient(properties)

        // Assert
        assertTrue(client is OllamaChatModel)
    }

    @Test
    fun `createClient should return OpenAiChatModel for OTHER provider`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns LangChainClientProvider.OTHER.name.lowercase()
        every { properties.baseUrl } returns "https://api.other.com"
        every { properties.apiKey } returns "other-api-key"
        every { properties.model } returns "other-model"
        every { properties.maxTokens } returns 800
        every { properties.temperature } returns 0.8
        every { properties.topP } returns 0.95
        every { properties.topK } returns 60
        every { properties.format } returns "json_object"

        // Act
        val client = LangChainChatModelFactory.createClient(properties)

        // Assert
        assertTrue(client is OpenAiChatModel)
    }

    @Test
    fun `createClient should throw IllegalArgumentException for unknown provider`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns "unknown_provider"
        every { properties.baseUrl } returns "https://api.unknown.com"
        every { properties.apiKey } returns "unknown-api-key"
        every { properties.model } returns "unknown-model"
        every { properties.maxTokens } returns 500
        every { properties.temperature } returns 0.9
        every { properties.topP } returns 0.99
        every { properties.topK } returns 70
        every { properties.format } returns "plain"

        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                LangChainChatModelFactory.createClient(properties)
            }
        assertEquals("Unknown model client properties: $properties", exception.message)
    }

    @Test
    fun `createClient should throw IllegalArgumentException when provider is empty`() {
        // Arrange
        val properties = mockk<ModelClientProperties>()
        every { properties.provider } returns ""
        every { properties.baseUrl } returns "https://api.null.com"
        every { properties.apiKey } returns "null-api-key"
        every { properties.model } returns "null-model"
        every { properties.maxTokens } returns 500
        every { properties.temperature } returns 0.9
        every { properties.topP } returns 0.99
        every { properties.topK } returns 70
        every { properties.format } returns "plain"

        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                LangChainChatModelFactory.createClient(properties)
            }
        assertEquals("Unknown model client properties: $properties", exception.message)
    }
}
