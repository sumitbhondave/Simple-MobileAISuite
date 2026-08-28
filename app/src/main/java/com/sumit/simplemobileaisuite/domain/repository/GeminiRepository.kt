package com.sumit.simplemobileaisuite.domain.repository

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the AI capabilities for the Gemini model.
 * Follows the Dependency Inversion Principle (SOLID).
 */
interface GeminiRepository {
    /**
     * Generates a streaming response for a given text prompt.
     */
    fun generateContentStream(prompt: String): Flow<String>

    /**
     * Generates a streaming response for a multi-modal prompt (text + image).
     */
    fun generateContentWithImageStream(prompt: String, bitmap: Bitmap): Flow<String>
}
