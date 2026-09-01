package com.sumit.simplemobileaisuite.ui.screens.gemini

import android.content.Context
import android.graphics.Bitmap
import app.cash.turbine.test
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.domain.usecase.GetGeminiResponseUseCase
import com.sumit.simplemobileaisuite.ui.screens.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GeminiViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getGeminiResponseUseCase: GetGeminiResponseUseCase
    private lateinit var context: Context
    private lateinit var viewModel: GeminiViewModel

    @Before
    fun setup() {
        getGeminiResponseUseCase = mockk()
        context = mockk(relaxed = true)
        every { context.getString(R.string.gemini_default_response) } returns "Default"
        viewModel = GeminiViewModel(getGeminiResponseUseCase, context)
    }

    @Test
    fun `askQuestion should update state with streaming response`() = runTest {
        val prompt = "Hello"
        val response1 = "Hi"
        val response2 = " there"
        every { getGeminiResponseUseCase.executeTextQuery(prompt) } returns flowOf(
            response1,
            response2
        )

        viewModel.askQuestion(prompt)

        viewModel.uiState.test {
            val finalItem = expectMostRecentItem()
            assertEquals("Hi there", finalItem.responseText)
            assertFalse(finalItem.isLoading)
        }
    }

    @Test
    fun `askQuestionWithImage should update state with streaming response`() = runTest {
        val prompt = "What's this?"
        val bitmap = mockk<Bitmap>()
        val response = "An image"
        every {
            getGeminiResponseUseCase.executeImageQuery(
                prompt,
                bitmap
            )
        } returns flowOf(response)

        viewModel.askQuestionWithImage(prompt, bitmap)

        viewModel.uiState.test {
            val finalItem = expectMostRecentItem()
            assertEquals("An image", finalItem.responseText)
            assertFalse(finalItem.isLoading)
        }
    }
}
