package com.sumit.simplemobileaisuite.data.repository

import android.graphics.Bitmap
import app.cash.turbine.test
import com.sumit.simplemobileaisuite.data.datasource.remote.GeminiRemoteDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeminiRepositoryImplTest {

    private lateinit var remoteDataSource: GeminiRemoteDataSource
    private lateinit var repository: GeminiRepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = mockk()
        repository = GeminiRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `generateContentStream should delegate to remote data source`() = runTest {
        val prompt = "Hello"
        val expectedResponse = "Hi"
        every { remoteDataSource.generateContentStream(prompt) } returns flowOf(expectedResponse)

        repository.generateContentStream(prompt).test {
            assertEquals(expectedResponse, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `generateContentWithImageStream should delegate to remote data source`() = runTest {
        val prompt = "What is this?"
        val bitmap = mockk<Bitmap>()
        val expectedResponse = "An image"
        every { remoteDataSource.generateContentWithImageStream(prompt, bitmap) } returns flowOf(
            expectedResponse
        )

        repository.generateContentWithImageStream(prompt, bitmap).test {
            assertEquals(expectedResponse, awaitItem())
            awaitComplete()
        }
    }
}
