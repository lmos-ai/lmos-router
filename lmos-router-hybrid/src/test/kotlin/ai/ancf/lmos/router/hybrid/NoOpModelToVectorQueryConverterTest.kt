// SPDX-FileCopyrightText: 2024 Deutsche Telekom AG
//
// SPDX-License-Identifier: Apache-2.0

package ai.ancf.lmos.router.hybrid

import ai.ancf.lmos.router.core.Context
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NoOpModelToVectorQueryConverterTest {
    @Test
    fun `test convert method returns correct VectorSearchClientRequest`() {
        // Arrange
        val modelResponse = "test response"
        val context = mockk<Context>()
        val converter = NoOpModelToVectorQueryConverter()

        // Act
        val result = converter.convert(modelResponse, context)

        // Assert
        assertEquals(modelResponse, result.query)
        assertEquals(context, result.context)
    }
}
