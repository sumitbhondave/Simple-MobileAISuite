package com.sumit.simplemobileaisuite.ui.screens.offline_chat

import android.content.Context
import app.cash.turbine.test
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import com.sumit.simplemobileaisuite.ui.screens.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OfflineChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: OfflineChatRepository
    private lateinit var context: Context
    private lateinit var viewModel: OfflineChatViewModel
    private val partialResults = MutableSharedFlow<Pair<String, Boolean>>()
    private val offlineLLMStatus = MutableStateFlow<OfflineLLMStatus>(OfflineLLMStatus.Idle)

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { repository.partialResults } returns partialResults
        every { repository.offlineLLMStatus } returns offlineLLMStatus
        viewModel = OfflineChatViewModel(repository, context)
    }

    @Test
    fun `initialization should trigger repository initialize`() {
        verify { repository.initialize() }
    }

    @Test
    fun `sendMessage should update chat history and call repository`() = runTest {
        val prompt = "Hello"
        viewModel.sendMessage(prompt)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            val messages = state.messages
            assertEquals(2, messages.size)
            assertEquals(prompt, messages[0].text)
            assertEquals(true, messages[0].isFromUser)
            assertEquals("", messages[1].text)
            assertEquals(false, messages[1].isFromUser)
            assertEquals(true, state.isGenerating)
        }
        verify { repository.generateResponse(prompt) }
    }

    @Test
    fun `receiving partial results with isDone true should stop generating`() = runTest {
        viewModel.sendMessage("Hello")

        partialResults.emit("Hi" to true)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isGenerating)
        }
    }
}
