// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid

import ai.ancf.lmos.router.core.Context
import ai.ancf.lmos.router.vector.VectorSearchClientRequest

/**
 * A model to vector query converter. Converts a model response to a vector search client request.
 */
abstract class ModelToVectorQueryConverter {
    abstract fun convert(
        modelResponse: String,
        context: Context,
    ): VectorSearchClientRequest
}

/**
 * A no-op model to vector query converter.
 */
class NoOpModelToVectorQueryConverter : ModelToVectorQueryConverter() {
    override fun convert(
        modelResponse: String,
        context: Context,
    ): VectorSearchClientRequest {
        return VectorSearchClientRequest(query = modelResponse, context = context)
    }
}
