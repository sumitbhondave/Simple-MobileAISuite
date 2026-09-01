package com.sumit.simplemobileaisuite.ui.screens.smart_chat

import android.content.Context
import app.cash.turbine.test
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import com.sumit.simplemobileaisuite.domain.usecase.GenerateSmartResponseUseCase
import com.sumit.simplemobileaisuite.ui.screens.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SmartChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var generateSmartResponse: GenerateSmartResponseUseCase
    private lateinit var offlineChatRepository: OfflineChatRepository
    private lateinit var context: Context
    private lateinit var viewModel: SmartChatViewModel
    private val offlineLLMStatus = MutableStateFlow<OfflineLLMStatus>(OfflineLLMStatus.Idle)

    @Before
    fun setup() {
        generateSmartResponse = mockk()
        offlineChatRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { offlineChatRepository.offlineLLMStatus } returns offlineLLMStatus
        viewModel = SmartChatViewModel(generateSmartResponse, offlineChatRepository, context)
    }

    @Test
    fun `initialization should trigger repository initialize and sync status`() = runTest {
        verify { offlineChatRepository.initialize() }

        offlineLLMStatus.emit(OfflineLLMStatus.Ready)
        assertEquals(OfflineLLMStatus.Ready, viewModel.uiState.value.offlineLLMStatus)
    }

    @Test
    fun `sendMessage should update state and call use case`() = runTest {
        val prompt = "Hello"
        every { generateSmartResponse(prompt) } returns flowOf(SmartResponse.TextChunk("Hi"))

        viewModel.sendMessage(prompt)

        viewModel.uiState.test {
            val finalState = expectMostRecentItem()
            assertEquals(2, finalState.messages.size)
            assertEquals(prompt, finalState.messages[0].text)
            assertEquals("Hi", finalState.messages[1].text)
            assertFalse(finalState.isGenerating)
        }
    }
}
