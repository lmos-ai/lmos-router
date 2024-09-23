// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

import ai.ancf.lmos.router.core.AgentRoutingSpecsResolver
import ai.ancf.lmos.router.core.Context
import ai.ancf.lmos.router.core.UserMessage
import ai.ancf.lmos.router.core.getOrThrow
import ai.ancf.lmos.router.llm.starter.LLMAgentRoutingSpecsResolverAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [LLMAgentRoutingSpecsResolverAutoConfiguration::class, OpenAiAutoConfiguration::class])
class SpringLLMAgentResolverFlow(
    @Autowired private val agentResolver: AgentRoutingSpecsResolver,
) {
    @Test
    fun `test agent detection using openai`() {
        require(System.getenv("OPENAI_API_KEY") != null) {
            "Please set the OPENAI_API_KEY environment variable to run the tests"
        }
        val context = Context(emptyList())
        val input = UserMessage("I want to buy a car")
        val result = agentResolver.resolve(context, input).getOrThrow()

        assert(result?.name == "offer-agent")
    }
}
