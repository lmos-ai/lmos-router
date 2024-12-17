// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.llm

import ai.ancf.lmos.router.core.*
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel.OpenAiChatModelBuilder

/**
 * A model client that uses Langchain4j(https://docs.langchain4j.dev/) to call a language model.
 *
 * @param chatLanguageModel The language model.
 */
class LangChainModelClient(
    private val chatLanguageModel: ChatLanguageModel,
) : ModelClient {
    override fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException> {
        try {
            val response =
                chatLanguageModel.generate(
                    messages.map {
                        when (it) {
                            is UserMessage -> dev.langchain4j.data.message.UserMessage(it.content)
                            is AssistantMessage -> AiMessage(it.content)
                            is SystemMessage -> dev.langchain4j.data.message.SystemMessage(it.content)
                            else -> throw AgentRoutingSpecResolverException("Unknown message type")
                        }
                    },
                )
            return Success(AssistantMessage(response.content().text()))
        } catch (e: Exception) {
            return Failure(AgentRoutingSpecResolverException("Failed to call language model", e))
        }
    }
}

/**
 * A factory class to create a Langchain4j language model client. The factory creates a client based on the given properties.
 *
 * The factory supports the following providers:
 * - OPENAI
 * - ANTHROPIC
 * - GEMINI
 * - OLLAMA
 * - GROQ
 * - OTHER (for other providers which are have OpenAI compatible API)
 */
class LangChainChatModelFactory private constructor() {
    companion object {
        fun createClient(properties: ModelClientProperties): ChatLanguageModel {
            return when (properties.provider) {
                LangChainClientProvider.OPENAI.name.lowercase(),
                LangChainClientProvider.OTHER.name.lowercase(),
                -> {
                    OpenAiChatModelBuilder().baseUrl(properties.url)
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .responseFormat(properties.format)
                        .topP(properties.topP)
                        .build()
                }

                LangChainClientProvider.ANTHROPIC.name.lowercase() -> {
                    AnthropicChatModel.builder().baseUrl(properties.url)
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .topP(properties.topP)
                        .topK(properties.topK)
                        .build()
                }

                LangChainClientProvider.GEMINI.name.lowercase() -> {
                    GoogleAiGeminiChatModel.builder()
                        .apiKey(properties.apiKey)
                        .modelName(properties.model)
                        .maxOutputTokens(properties.maxTokens)
                        .temperature(properties.temperature)
                        .topK(properties.topK)
                        .topP(properties.topP)
                        .responseFormat(ResponseFormat.builder().type(ResponseFormatType.valueOf(properties.format)).build())
                        .build()
                }

                LangChainClientProvider.OLLAMA.name.lowercase() -> {
                    OllamaChatModel.builder().baseUrl(properties.url)
                        .modelName(properties.model)
                        .temperature(properties.temperature)
                        .build()
                }

                else -> {
                    throw IllegalArgumentException("Unknown model client properties: $properties")
                }
            }
        }
    }
}

enum class LangChainClientProvider {
    OPENAI,
    ANTHROPIC,
    GEMINI,
    OLLAMA,
    OTHER,
}
