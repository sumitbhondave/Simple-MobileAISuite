package com.sumit.simplemobileaisuite.domain.usecase

import android.graphics.Bitmap
import app.cash.turbine.test
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGeminiResponseUseCaseTest {

    private lateinit var geminiRepository: GeminiRepository
    private lateinit var useCase: GetGeminiResponseUseCase

    @Before
    fun setup() {
        geminiRepository = mockk()
        useCase = GetGeminiResponseUseCase(geminiRepository)
    }

    @Test
    fun `executeTextQuery should delegate to repository`() = runTest {
        val prompt = "Hello"
        val expectedResponse = "Hi there"
        every { geminiRepository.generateContentStream(prompt) } returns flowOf(expectedResponse)

        useCase.executeTextQuery(prompt).test {
            assertEquals(expectedResponse, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `executeImageQuery should delegate to repository`() = runTest {
        val prompt = "What is this?"
        val bitmap = mockk<Bitmap>()
        val expectedResponse = "An image"
        every { geminiRepository.generateContentWithImageStream(prompt, bitmap) } returns flowOf(
            expectedResponse
        )

        useCase.executeImageQuery(prompt, bitmap).test {
            assertEquals(expectedResponse, awaitItem())
            awaitComplete()
        }
    }
}
