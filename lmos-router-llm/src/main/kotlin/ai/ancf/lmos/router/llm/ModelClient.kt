// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.llm

import ai.ancf.lmos.router.core.*

/**
 * The [ModelClient] interface represents a client that can call a model.
 *
 * The [call] method calls the model with the given messages.
 *
 * @see ChatMessage
 * @see AgentRoutingSpecResolverException
 * @see Result
 */
interface ModelClient {
    fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException>
}

/**
 * The [DefaultModelClient] class is a default implementation of the [ModelClient] interface.
 *
 * The [call] method calls the OpenAI model with the given messages.
 *
 * @param defaultModelClientProperties The properties for the default model client.
 */
class DefaultModelClient(
    private val defaultModelClientProperties: DefaultModelClientProperties,
    private val delegate: LangChainModelClient =
        LangChainModelClient(LangChainChatModelFactory.createClient(defaultModelClientProperties)),
) : ModelClient {
    override fun call(messages: List<ChatMessage>): Result<ChatMessage, AgentRoutingSpecResolverException> {
        return delegate.call(messages)
    }
}

abstract class ModelClientProperties(
    open val provider: String,
    open val apiKey: String?,
    open val url: String,
    open val model: String,
    open val maxTokens: Int,
    open val temperature: Double,
    open val format: String,
    open val topK: Int = 0,
    open val topP: Double = 0.0,
)

/**
 * The [DefaultModelClientProperties] data class represents the properties for the default model client.
 *
 * @param openAiUrl The OpenAI URL.
 * @param openAiApiKey The OpenAI API key.
 * @param model The model.
 * @param maxTokens The maximum number of tokens.
 * @param temperature The temperature.
 * @param format The format.
 */
data class DefaultModelClientProperties(
    val openAiUrl: String = "https://api.openai.com/v1/chat/completions",
    val openAiApiKey: String,
    override val model: String = "gpt-4o-mini",
    override val maxTokens: Int = 200,
    override val temperature: Double = 0.0,
    override val format: String = "json_object",
    override val apiKey: String? = openAiApiKey,
    override val url: String = openAiUrl,
    override val provider: String = "openai",
) : ModelClientProperties(
        provider,
        apiKey,
        url,
        model,
        maxTokens,
        temperature,
        format,
    )
