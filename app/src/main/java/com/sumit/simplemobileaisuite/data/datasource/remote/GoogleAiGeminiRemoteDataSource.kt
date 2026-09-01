package com.sumit.simplemobileaisuite.data.datasource.remote

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sumit.simplemobileaisuite.BuildConfig
import com.sumit.simplemobileaisuite.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GeminiRemoteDataSource using Google AI SDK.
 */
@Singleton
class GoogleAiGeminiRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : GeminiRemoteDataSource {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override fun generateContentStream(prompt: String): Flow<String> {
        return try {
            generativeModel.generateContentStream(prompt).map { it.text ?: "" }
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.failed_generate_content, e.localizedMessage))
        }
    }

    override fun generateContentWithImageStream(prompt: String, bitmap: Bitmap): Flow<String> {
        val content = content {
            image(bitmap)
            text(prompt)
        }
        return try {
            generativeModel.generateContentStream(content).map { it.text ?: "" }
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.failed_generate_content, e.localizedMessage))
        }
    }
}
