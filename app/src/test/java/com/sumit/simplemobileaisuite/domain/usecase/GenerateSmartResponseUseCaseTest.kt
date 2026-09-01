package com.sumit.simplemobileaisuite.domain.usecase

import app.cash.turbine.test
import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import com.sumit.simplemobileaisuite.domain.repository.SmartChatRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GenerateSmartResponseUseCaseTest {

    private lateinit var repository: SmartChatRepository
    private lateinit var useCase: GenerateSmartResponseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GenerateSmartResponseUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository`() = runTest {
        val prompt = "Hello"
        val expectedResponse = SmartResponse.TextChunk("Hi")
        every { repository.getSmartResponse(prompt) } returns flowOf(expectedResponse)

        useCase(prompt).test {
            assertEquals(expectedResponse, awaitItem())
            awaitComplete()
        }
    }
}
