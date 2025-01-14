// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.router.llm

import io.mockk.every
import io.mockk.mockk
import org.eclipse.lmos.router.core.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultModelClientTest {
    private lateinit var defaultModelClientProperties: DefaultModelClientProperties
    private lateinit var defaultModelClient: DefaultModelClient
    private lateinit var delegate: LangChainModelClient

    @BeforeEach
    fun setUp() {
        defaultModelClientProperties =
            DefaultModelClientProperties(
                openAiApiKey = "fake-api-key",
                openAiUrl = "https://api.openai.com/v1/chat/completions",
                model = "gpt-4o-mini",
                maxTokens = 200,
                temperature = 0.0,
                format = "json_object",
            )

        delegate = mockk()
        defaultModelClient = DefaultModelClient(defaultModelClientProperties, delegate)
    }

    @Test
    fun `call should return AssistantMessage when valid messages are passed`() {
        val messages =
            listOf(
                UserMessage("Hello, how can I assist you today?"),
                AssistantMessage("I am here to help you."),
            )

        every { delegate.call(any()) } returns Success(AssistantMessage("This is a response from the assistant."))
        val result = defaultModelClient.call(messages)

        assertEquals(result, Success(AssistantMessage("This is a response from the assistant.")))
    }

    @Test
    fun `call should throw AgentRoutingSpecResolverException for API call failure`() {
        val messages =
            listOf(
                UserMessage("Hello, how can I assist you today?"),
            )

        every { delegate.call(any()) } returns Failure(AgentRoutingSpecResolverException("Failed to call language model"))
        val result = defaultModelClient.call(messages)

        assert(result is Failure)
        assert((result as Failure).exceptionOrNull()?.message == "Failed to call language model")
    }
}
