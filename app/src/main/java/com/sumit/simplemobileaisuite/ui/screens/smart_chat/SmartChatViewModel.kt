package com.sumit.simplemobileaisuite.ui.screens.smart_chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.domain.model.ChatMessage
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import com.sumit.simplemobileaisuite.domain.usecase.GenerateSmartResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified State for the Smart Chat screen.
 */
data class SmartChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val offlineLLMStatus: OfflineLLMStatus = OfflineLLMStatus.Idle,
    val headerStatusMessage: String? = null
)

@HiltViewModel
class SmartChatViewModel @Inject constructor(
    private val generateSmartResponse: GenerateSmartResponseUseCase,
    private val offlineChatRepository: OfflineChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartChatState())
    val uiState: StateFlow<SmartChatState> = _uiState.asStateFlow()

    init {
        // Automatically warm up the local AI engine as soon as the user enters the screen
        offlineChatRepository.initialize()

        // Sync the hardware status from the repository into our unified UI state
        viewModelScope.launch {
            offlineChatRepository.offlineLLMStatus.collect { status ->
                _uiState.update { it.copy(offlineLLMStatus = status) }
            }
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.toMutableList().apply {
                add(ChatMessage(text = prompt, isFromUser = true))
                add(ChatMessage(text = "", isFromUser = false))
            }
            currentState.copy(
                messages = updatedMessages,
                isGenerating = true,
                headerStatusMessage = null // Reset runtime status on new query
            )
        }

        viewModelScope.launch {
            generateSmartResponse(prompt)
                .onCompletion {
                    _uiState.update { it.copy(isGenerating = false) }
                }
                .collect { response ->
                    when (response) {
                        is SmartResponse.TextChunk -> {
                            appendChunkToLastMessage(response.text)
                        }

                        is SmartResponse.SystemEvent -> {
                            handleSystemEvent(response)
                        }
                    }
                }
        }
    }

    private fun handleSystemEvent(event: SmartResponse.SystemEvent) {
        val message = when (event) {
            is SmartResponse.SystemEvent.LocalAiNotInitialized ->
                context.getString(R.string.local_ai_not_init_fallback)

            is SmartResponse.SystemEvent.LocalAiError ->
                context.getString(R.string.local_ai_error_fallback, event.errorMessage)

            is SmartResponse.SystemEvent.CloudFallbackStarted ->
                context.getString(R.string.cloud_fallback_active)

            is SmartResponse.SystemEvent.CloudError ->
                context.getString(R.string.error_prefix, event.errorMessage)

            is SmartResponse.SystemEvent.HardwareUnsupported ->
                context.getString(R.string.hardware_rejected_opencl)

            is SmartResponse.SystemEvent.OfflineNoLocalFallback ->
                context.getString(R.string.offline_no_local_fallback)
        }
        _uiState.update { it.copy(headerStatusMessage = message) }
    }

    private fun appendChunkToLastMessage(chunk: String) {
        _uiState.update { currentState ->
            if (currentState.messages.isEmpty()) return@update currentState

            val updatedMessages = currentState.messages.toMutableList()
            val lastIndex = updatedMessages.lastIndex
            val lastMessage = updatedMessages[lastIndex]

            updatedMessages[lastIndex] = lastMessage.copy(text = lastMessage.text + chunk)
            currentState.copy(messages = updatedMessages)
        }
    }
}
