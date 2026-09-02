package com.sumit.simplemobileaisuite.ui.screens.gemini

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.core.util.chunkedByTime
import com.sumit.simplemobileaisuite.domain.model.ChatMessage
import com.sumit.simplemobileaisuite.domain.usecase.GetGeminiResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified State for the Gemini screen.
 */
data class GeminiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel for the Gemini AI feature.
 * Uses Hilt for dependency injection and communicates with [GetGeminiResponseUseCase].
 */
@HiltViewModel
class GeminiViewModel @Inject constructor(
    private val getGeminiResponseUseCase: GetGeminiResponseUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeminiState())
    val uiState: StateFlow<GeminiState> = _uiState.asStateFlow()

    /**
     * Sends a text-only prompt to Gemini.
     */
    fun askQuestion(prompt: String) {
        if (prompt.isBlank()) return

        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.toMutableList().apply {
                add(ChatMessage(text = prompt, isFromUser = true))
                add(ChatMessage(text = "", isFromUser = false))
            }
            currentState.copy(messages = updatedMessages, isLoading = true)
        }

        viewModelScope.launch {
            val fullResponse = StringBuilder()
            try {
                getGeminiResponseUseCase.executeTextQuery(prompt)
                    .chunkedByTime(100)
                    .collect { batchedChunk ->
                        fullResponse.append(batchedChunk)
                        appendChunkToLastMessage(batchedChunk)
                    }
            } catch (e: Exception) {
                appendChunkToLastMessage(
                    context.getString(R.string.error_prefix, e.localizedMessage)
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Sends a multi-modal prompt (text + image) to Gemini.
     */
    fun askQuestionWithImage(prompt: String, bitmap: Bitmap) {
        _uiState.update { currentState ->
            val updatedMessages = currentState.messages.toMutableList().apply {
                add(ChatMessage(text = prompt, isFromUser = true))
                add(ChatMessage(text = "", isFromUser = false))
            }
            currentState.copy(messages = updatedMessages, isLoading = true)
        }

        viewModelScope.launch {
            try {
                getGeminiResponseUseCase.executeImageQuery(prompt, bitmap)
                    .chunkedByTime(100)
                    .collect { batchedChunk ->
                        appendChunkToLastMessage(batchedChunk)
                    }
            } catch (e: Exception) {
                appendChunkToLastMessage(
                    context.getString(R.string.error_prefix, e.localizedMessage)
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
}
