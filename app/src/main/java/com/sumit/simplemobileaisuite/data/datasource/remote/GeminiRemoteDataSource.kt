package com.sumit.simplemobileaisuite.data.datasource.remote

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * Interface for remote Gemini AI data source.
 */
interface GeminiRemoteDataSource {
    fun generateContentStream(prompt: String): Flow<String>
    fun generateContentWithImageStream(prompt: String, bitmap: Bitmap): Flow<String>
}
