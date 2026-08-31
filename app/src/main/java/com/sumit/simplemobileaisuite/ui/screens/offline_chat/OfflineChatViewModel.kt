package com.sumit.simplemobileaisuite.ui.screens.offline_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class to hold chat messages
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

/**
 * ViewModel for the Offline Chat feature powered by Gemma.
 */
@HiltViewModel
class OfflineChatViewModel @Inject constructor(
    private val repository: OfflineChatRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val offlineLLMStatus: StateFlow<OfflineLLMStatus> = repository.offlineLLMStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OfflineLLMStatus.Idle
        )

    init {
        // Trigger initialization
        repository.initialize()

        // Listen to the AI streaming its response
        viewModelScope.launch(Dispatchers.Main) {
            repository.partialResults.collect { (chunk, isDone) ->
                appendChunkToLastMessage(chunk)
                if (isDone) {
                    _isGenerating.value = false
                }
            }
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _isGenerating.value) return

        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(text = prompt, isFromUser = true))
        currentList.add(ChatMessage(text = "", isFromUser = false))
        _chatMessages.value = currentList

        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.generateResponse(prompt)
            } catch (e: Exception) {
                // If the GPU rejects the model, print the error in the chat bubble!
                appendChunkToLastMessage("Hardware Error: Your device's GPU does not support the required compute shaders (Work group size 512).")
                _isGenerating.value = false
            }
        }
    }

    private fun appendChunkToLastMessage(chunk: String) {
        val currentList = _chatMessages.value.toMutableList()
        if (currentList.isNotEmpty()) {
            val lastMessage = currentList.last()
            // Append the new text chunk from the GPU to the AI's message
            val updatedMessage = lastMessage.copy(text = lastMessage.text + chunk)
            currentList[currentList.lastIndex] = updatedMessage
            _chatMessages.value = currentList
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Extremely important to free up the GPU RAM when the screen dies
        repository.close()
    }
}
