// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid

import ai.ancf.lmos.router.core.AgentRoutingSpec
import ai.ancf.lmos.router.core.AgentRoutingSpecsProvider
import ai.ancf.lmos.router.core.AssistantMessage
import ai.ancf.lmos.router.core.ChatMessage
import ai.ancf.lmos.router.core.Context
import ai.ancf.lmos.router.core.SpecFilter
import ai.ancf.lmos.router.core.Success
import ai.ancf.lmos.router.core.UserMessage
import ai.ancf.lmos.router.core.getOrThrow
import ai.ancf.lmos.router.llm.ModelClient
import ai.ancf.lmos.router.llm.ModelPromptProvider
import ai.ancf.lmos.router.vector.VectorSearchClient
import ai.ancf.lmos.router.vector.VectorSearchClientResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HybridAgentRoutingSpecsResolverTest {
    private lateinit var agentRoutingSpecsProvider: AgentRoutingSpecsProvider
    private lateinit var modelClient: ModelClient
    private lateinit var modelPromptProvider: ModelPromptProvider
    private lateinit var vectorSearchClient: VectorSearchClient
    private lateinit var modelToVectorQueryConverter: ModelToVectorQueryConverter
    private lateinit var hybridAgentRoutingSpecsResolver: HybridAgentRoutingSpecsResolver

    @BeforeEach
    fun setUp() {
        agentRoutingSpecsProvider = mockk()
        modelClient = mockk()
        modelPromptProvider = mockk()
        vectorSearchClient = mockk()
        modelToVectorQueryConverter = spyk<NoOpModelToVectorQueryConverter>()
        hybridAgentRoutingSpecsResolver =
            HybridAgentRoutingSpecsResolver(
                agentRoutingSpecsProvider,
                modelClient,
                modelPromptProvider,
                vectorSearchClient,
                modelToVectorQueryConverter,
            )
    }

    @Test
    fun `resolve without filters returns agent spec`() {
        // Arrange
        val context = mockk<Context> { every { previousMessages } returns listOf<ChatMessage>() }
        val input = mockk<UserMessage>()
        val agentRoutingSpec1 = mockk<AgentRoutingSpec> { every { name } returns "Agent1" }
        val agentSpecs = setOf(agentRoutingSpec1)
        val vectorSearchClientResponse = mockk<VectorSearchClientResponse> { every { agentName } returns "Agent1" }

        every { agentRoutingSpecsProvider.provide() } returns Success(agentSpecs)
        every { modelPromptProvider.providePrompt(context, agentSpecs, input) } returns Success("Model prompt")
        every { modelClient.call(any()) } returns Success(AssistantMessage("Model response"))
        every { vectorSearchClient.find(any(), any()) } returns Success(vectorSearchClientResponse)

        // Act
        val result = hybridAgentRoutingSpecsResolver.resolve(context, input)

        // Assert
        assertTrue(result is Success)
        assertEquals(agentRoutingSpec1, result.getOrThrow())
        verify { agentRoutingSpecsProvider.provide() }
        verify { modelPromptProvider.providePrompt(context, agentSpecs, input) }
        verify { modelClient.call(any()) }
        verify { vectorSearchClient.find(any(), any()) }
        verify(exactly = 1) { modelToVectorQueryConverter.convert(any(), any()) }
    }

    @Test
    fun `resolve with filters returns agent spec`() {
        // Arrange
        val filters = setOf<SpecFilter>(mockk())
        val context = mockk<Context> { every { previousMessages } returns listOf<ChatMessage>() }
        val input = mockk<UserMessage>()
        val agentRoutingSpec1 = mockk<AgentRoutingSpec> { every { name } returns "Agent1" }
        val agentSpecs = setOf(agentRoutingSpec1)
        val vectorSearchClientResponse = mockk<VectorSearchClientResponse> { every { agentName } returns "Agent1" }

        every { agentRoutingSpecsProvider.provide(filters) } returns Success(agentSpecs)
        every { modelPromptProvider.providePrompt(context, agentSpecs, input) } returns Success("Model prompt")
        every { modelClient.call(any()) } returns Success(AssistantMessage("Model response"))
        every { vectorSearchClient.find(any(), any()) } returns Success(vectorSearchClientResponse)

        // Act
        val result = hybridAgentRoutingSpecsResolver.resolve(filters, context, input)

        // Assert
        assertTrue(result is Success)
        assertEquals(agentRoutingSpec1, result.getOrThrow())
        verify { agentRoutingSpecsProvider.provide(filters) }
        verify { modelPromptProvider.providePrompt(context, agentSpecs, input) }
        verify { modelClient.call(any()) }
    }
}
