package com.sumit.simplemobileaisuite.ui.screens.gemini

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gemini AI feature.
 * Uses Hilt for dependency injection and communicates with [GeminiRepository].
 */
@HiltViewModel
class GeminiViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow("Ask Gemini something...")
    val uiState: StateFlow<String> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Sends a text-only prompt to Gemini.
     */
    fun askQuestion(prompt: String) {
        if (prompt.isBlank()) return

        _uiState.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val stringBuilder = StringBuilder()
            try {
                geminiRepository.generateContentStream(prompt).collect { chunk ->
                    stringBuilder.append(chunk)
                    _uiState.value = stringBuilder.toString()
                }
            } catch (e: Exception) {
                _uiState.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sends a multi-modal prompt (text + image) to Gemini.
     */
    fun askQuestionWithImage(prompt: String, bitmap: Bitmap) {
        _uiState.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val stringBuilder = StringBuilder()
            try {
                geminiRepository.generateContentWithImageStream(prompt, bitmap).collect { chunk ->
                    stringBuilder.append(chunk)
                    _uiState.value = stringBuilder.toString()
                }
            } catch (e: Exception) {
                _uiState.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
