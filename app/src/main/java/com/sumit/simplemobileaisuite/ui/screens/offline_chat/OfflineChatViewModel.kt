package com.sumit.simplemobileaisuite.ui.screens.offline_chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.core.util.chunkedByTime
import com.sumit.simplemobileaisuite.domain.model.ChatMessage
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified State for the Offline Chat screen.
 */
data class OfflineChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val offlineLLMStatus: OfflineLLMStatus = OfflineLLMStatus.Idle
)

/**
 * ViewModel for the Offline Chat feature powered by Gemma.
 */
@HiltViewModel
class OfflineChatViewModel @Inject constructor(
    private val repository: OfflineChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineChatState())
    val uiState: StateFlow<OfflineChatState> = _uiState.asStateFlow()

    init {
        // Trigger initialization
        repository.initialize()

        // Sync hardware status
        viewModelScope.launch {
            repository.offlineLLMStatus.collect { status ->
                _uiState.update { it.copy(offlineLLMStatus = status) }
            }
        }

        // Listen to the AI streaming its response with batching
        viewModelScope.launch(Dispatchers.Main) {
            repository.partialResults
                .map { it.first } // Extract just the text chunk for batching
                .chunkedByTime(100)
                .collect { batchedChunk ->
                    appendChunkToLastMessage(batchedChunk)
                }
        }

        // Separately monitor the completion flag
        viewModelScope.launch {
            repository.partialResults.collect { (_, isDone) ->
                if (isDone) {
                    _uiState.update { it.copy(isGenerating = false) }
                }
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.generateResponse(prompt)
            } catch (e: Exception) {
                appendChunkToLastMessage(context.getString(R.string.hardware_error_gpu))
                _uiState.update { it.copy(isGenerating = false) }
            }
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

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}
