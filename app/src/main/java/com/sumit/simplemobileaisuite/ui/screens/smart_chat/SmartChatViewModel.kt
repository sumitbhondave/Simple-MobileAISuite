package com.sumit.simplemobileaisuite.ui.screens.smart_chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.core.util.chunkedByTime
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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
    val offlineLLMStatus: OfflineLLMStatus = OfflineLLMStatus.Idle
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
                isGenerating = true
            )
        }

        viewModelScope.launch {
            generateSmartResponse(prompt)
                .map { response ->
                    // Map domain events to localized strings in the ViewModel
                    when (response) {
                        is SmartResponse.TextChunk -> response.text
                        is SmartResponse.SystemEvent -> mapSystemEventToString(response) ?: ""
                    }
                }
                .chunkedByTime(100) // Keep the UI stable and responsive
                .catch { error ->
                    appendChunkToLastMessage(
                        context.getString(
                            R.string.system_critical_error,
                            error.message
                        )
                    )
                }
                .onCompletion {
                    _uiState.update { it.copy(isGenerating = false) }
                }
                .collect { batchedText ->
                    if (batchedText.isNotEmpty()) {
                        appendChunkToLastMessage(batchedText)
                    }
                }
        }
    }

    private fun mapSystemEventToString(event: SmartResponse.SystemEvent): String? {
        return when (event) {
            is SmartResponse.SystemEvent.LocalAiNotInitialized ->
                context.getString(R.string.local_ai_not_init_fallback)

            is SmartResponse.SystemEvent.LocalAiError ->
                context.getString(R.string.local_ai_error_fallback, event.errorMessage)

            is SmartResponse.SystemEvent.CloudFallbackStarted ->
                null

            is SmartResponse.SystemEvent.CloudError ->
                context.getString(R.string.error_prefix, event.errorMessage)

            is SmartResponse.SystemEvent.HardwareUnsupported ->
                context.getString(R.string.hardware_rejected_opencl)

            is SmartResponse.SystemEvent.OfflineNoLocalFallback ->
                context.getString(R.string.offline_no_local_fallback)
        }
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
