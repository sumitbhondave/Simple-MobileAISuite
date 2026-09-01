package com.sumit.simplemobileaisuite.domain.usecase

import android.graphics.Bitmap
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to interact with the Gemini AI model.
 * Orchestrates text and multi-modal requests.
 */
class GetGeminiResponseUseCase(
    private val geminiRepository: GeminiRepository
) {
    /**
     * Generates a streaming response for a text-only prompt.
     */
    fun executeTextQuery(prompt: String): Flow<String> {
        return geminiRepository.generateContentStream(prompt)
    }

    /**
     * Generates a streaming response for a text + image prompt.
     */
    fun executeImageQuery(prompt: String, bitmap: Bitmap): Flow<String> {
        return geminiRepository.generateContentWithImageStream(prompt, bitmap)
    }
}
