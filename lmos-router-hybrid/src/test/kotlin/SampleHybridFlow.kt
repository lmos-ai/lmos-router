// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

import ai.ancf.lmos.router.core.*
import ai.ancf.lmos.router.hybrid.HybridAgentRoutingSpecsResolver
import ai.ancf.lmos.router.hybrid.ModelToVectorQueryConverter
import ai.ancf.lmos.router.llm.DefaultModelClient
import ai.ancf.lmos.router.llm.DefaultModelClientProperties
import ai.ancf.lmos.router.llm.ExternalModelPromptProvider
import ai.ancf.lmos.router.vector.DefaultEmbeddingClient
import ai.ancf.lmos.router.vector.DefaultEmbeddingClientProperties
import ai.ancf.lmos.router.vector.DefaultVectorClient
import ai.ancf.lmos.router.vector.DefaultVectorClientProperties
import ai.ancf.lmos.router.vector.EmbeddingClient
import ai.ancf.lmos.router.vector.VectorSearchClientRequest
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.ollama.OllamaContainer

class SampleHybridFlow {
    @Test
    fun `test sample flow`() {
        val agentRoutingSpecsProvider =
            JsonAgentRoutingSpecsProvider(jsonFilePath = "src/test/resources/agentRoutingSpecs.json")
        val modelPromptProvider = ExternalModelPromptProvider(promptFilePath = "src/test/resources/prompt.txt")
        val modelClient =
            DefaultModelClient(
                DefaultModelClientProperties(
                    openAiApiKey = System.getenv("OPENAI_API_KEY") ?: throw Exception("OPENAI_API_KEY is not set"),
                ),
            )

        val converter =
            object : ModelToVectorQueryConverter() {
                override fun convert(
                    modelResponse: String,
                    context: Context,
                ): VectorSearchClientRequest {
                    val parsed = Json.decodeFromString<ModelResponse>(modelResponse)
                    return VectorSearchClientRequest(parsed.primaryRequirements.first(), context)
                }
            }

        val vectorClient =
            DefaultVectorClient(
                DefaultVectorClientProperties(
                    seedJsonFilePath = "src/test/resources/seed.json",
                ),
                embeddingClient,
            )

        val hybridAgentRoutingSpecsResolver =
            HybridAgentRoutingSpecsResolver(
                agentRoutingSpecsProvider,
                modelClient,
                modelPromptProvider,
                vectorClient,
                converter,
            )

        val context = Context(listOf(AssistantMessage("Hello")))

        // The input to test whether offer-agent is resolved
        val input = UserMessage("Everytime I try to pay. I get an error")
        val result = hybridAgentRoutingSpecsResolver.resolve(context, input)
        assert(result is Success)
        assert((result as Success).getOrNull()?.name == "payment-agent")
    }

    companion object {
        private lateinit var container: OllamaContainer
        private lateinit var embeddingClient: EmbeddingClient

        @JvmStatic
        @BeforeAll
        fun setup() {
            container = OllamaContainer("ollama/ollama:0.1.26")
            container.start()
            container.execInContainer(
                "ollama",
                "pull",
                DefaultEmbeddingClientProperties().model,
            )
            embeddingClient =
                DefaultEmbeddingClient(
                    HttpClient(),
                    DefaultEmbeddingClientProperties(container.endpoint + "/api/embeddings"),
                )
            val ping = embeddingClient.embed("Hello").getOrNull()
            require(ping != null) {
                "Ollama is not running. Please start Ollama to run this test. " +
                    "Also, make sure you have 'all-minilm' installed in your Ollama."
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDownAll() {
            container.stop()
        }
    }
}

@Serializable
data class ModelResponse(val primaryRequirements: List<String>)
