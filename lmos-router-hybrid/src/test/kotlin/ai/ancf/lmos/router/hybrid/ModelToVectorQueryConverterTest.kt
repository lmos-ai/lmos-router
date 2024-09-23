// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid

import ai.ancf.lmos.router.core.Context
import ai.ancf.lmos.router.vector.VectorSearchClientRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ModelToVectorQueryConverterTest {
    @Test
    fun `test convert method is called with correct parameters`() {
        // Arrange
        val modelResponse = "test response"
        val context = mockk<Context>()
        val mockConverter = mockk<ModelToVectorQueryConverter>()

        every { mockConverter.convert(modelResponse, context) } returns VectorSearchClientRequest(query = modelResponse, context = context)

        // Act
        val result = mockConverter.convert(modelResponse, context)

        // Assert
        verify { mockConverter.convert(modelResponse, context) }
        assertEquals(modelResponse, result.query)
        assertEquals(context, result.context)
    }
}
