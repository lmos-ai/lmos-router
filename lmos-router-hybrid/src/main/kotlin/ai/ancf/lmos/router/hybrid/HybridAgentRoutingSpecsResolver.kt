// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid

import ai.ancf.lmos.router.core.AgentRoutingSpec
import ai.ancf.lmos.router.core.AgentRoutingSpecResolverException
import ai.ancf.lmos.router.core.AgentRoutingSpecsProvider
import ai.ancf.lmos.router.core.AgentRoutingSpecsResolver
import ai.ancf.lmos.router.core.ChatMessage
import ai.ancf.lmos.router.core.Context
import ai.ancf.lmos.router.core.Failure
import ai.ancf.lmos.router.core.Result
import ai.ancf.lmos.router.core.SpecFilter
import ai.ancf.lmos.router.core.Success
import ai.ancf.lmos.router.core.SystemMessage
import ai.ancf.lmos.router.core.UserMessage
import ai.ancf.lmos.router.core.getOrThrow
import ai.ancf.lmos.router.llm.ModelClient
import ai.ancf.lmos.router.llm.ModelPromptProvider
import ai.ancf.lmos.router.vector.VectorSearchClient
import ai.ancf.lmos.router.vector.VectorSearchClientRequest
import org.slf4j.LoggerFactory

/**
 * A hybrid agent routing specs resolver that combines model-based and vector-based approaches.
 *
 * @property agentRoutingSpecsProvider The agent routing specs provider.
 * @property modelClient The model client.
 * @property modelPromptProvider The model prompt provider.
 * @property vectorSearchClient The vector search client.
 */
class HybridAgentRoutingSpecsResolver(
    override val agentRoutingSpecsProvider: AgentRoutingSpecsProvider,
    private val modelClient: ModelClient,
    private val modelPromptProvider: ModelPromptProvider,
    private val vectorSearchClient: VectorSearchClient,
    private val modelToVectorQueryConverter: ModelToVectorQueryConverter,
) : AgentRoutingSpecsResolver {
    private val log = LoggerFactory.getLogger(HybridAgentRoutingSpecsResolver::class.java)

    /**
     * Resolves the agent routing spec using a hybrid approach without filters.
     *
     * @param context The context.
     * @param input The user message.
     * @return The agent routing spec or an exception.
     */
    override fun resolve(
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> {
        log.info("Resolving agent routing spec using hybrid approach without filters")
        try {
            // Get the agent specs
            val agentSpecs = agentRoutingSpecsProvider.provide().getOrThrow()

            // Get the model prompt
            val modelPrompt = modelPromptProvider.providePrompt(context, agentSpecs, input).getOrThrow()

            // Create a list of messages
            val messages = mutableListOf<ChatMessage>(SystemMessage(modelPrompt))
            messages.addAll(context.previousMessages)
            messages.add(input)

            // Call the model
            val modelResponse = modelClient.call(messages).getOrThrow()

            // Create a vector search client request
            val vectorSearchClientRequest = modelToVectorQueryConverter.convert(modelResponse.content, context)

            // Find the agent name
            return vectorSearchClient.find(vectorSearchClientRequest, agentSpecs).getOrThrow()?.agentName.let {
                Success(agentSpecs.find { agentSpec -> agentSpec.name == it })
            }
        } catch (e: Exception) {
            return Failure(AgentRoutingSpecResolverException("Failed to resolve agent routing spec", e))
        }
    }

    /**
     * Resolves the agent routing spec using a hybrid approach with filters.
     *
     * @param filters The filters.
     * @param context The context.
     * @param input The user message.
     * @return The agent routing spec or an exception.
     */
    override fun resolve(
        filters: Set<SpecFilter>,
        context: Context,
        input: UserMessage,
    ): Result<AgentRoutingSpec?, AgentRoutingSpecResolverException> {
        log.info("Resolving agent routing spec using hybrid approach with filters")
        try {
            // Get the agent specs
            val agentSpecs = agentRoutingSpecsProvider.provide(filters).getOrThrow()

            // Get the model prompt
            val modelPrompt = modelPromptProvider.providePrompt(context, agentSpecs, input).getOrThrow()

            // Create a list of messages
            val messages = mutableListOf<ChatMessage>(SystemMessage(modelPrompt))
            messages.addAll(context.previousMessages)
            messages.add(input)

            // Call the model
            val modelResponse = modelClient.call(messages).getOrThrow()

            // Create a vector search client request
            val vectorSearchClientRequest = VectorSearchClientRequest(query = modelResponse.content, context = context)

            // Find the agent name
            return vectorSearchClient.find(vectorSearchClientRequest, agentSpecs).getOrThrow()?.agentName.let {
                Success(agentSpecs.find { agentSpec -> agentSpec.name == it })
            }
        } catch (e: Exception) {
            return Failure(AgentRoutingSpecResolverException("Failed to resolve agent routing spec", e))
        }
    }
}
