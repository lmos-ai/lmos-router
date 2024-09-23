// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid.starter

import ai.ancf.lmos.router.core.Failure
import ai.ancf.lmos.router.core.Result
import ai.ancf.lmos.router.core.Success
import ai.ancf.lmos.router.vector.VectorClientException
import ai.ancf.lmos.router.vector.VectorRouteConstants.Companion.AGENT_FIELD_NAME
import ai.ancf.lmos.router.vector.VectorSeedClient
import ai.ancf.lmos.router.vector.VectorSeedRequest
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore

/**
 * A Spring implementation of the VectorSeedClient.
 *
 * @param vectorStore The vector store to seed.
 */
class SpringVectorSeedClient(
    private val vectorStore: VectorStore,
) : VectorSeedClient {
    /**
     * Seeds the vector store with the given documents.
     *
     * @param documents The documents to seed the vector store with.
     * @return A result indicating success or failure.
     */
    override fun seed(documents: List<VectorSeedRequest>): Result<Unit, VectorClientException> {
        try {
            val vectorDocuments =
                documents.map {
                    Document(
                        it.text,
                        mapOf(AGENT_FIELD_NAME to it.agentName),
                    )
                }

            return Success(vectorStore.add(vectorDocuments))
        } catch (e: Exception) {
            return Failure(VectorClientException(e.message ?: "An error occurred while seeding the vector store"))
        }
    }
}
