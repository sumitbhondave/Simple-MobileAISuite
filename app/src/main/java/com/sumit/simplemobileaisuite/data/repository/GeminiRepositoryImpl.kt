package com.sumit.simplemobileaisuite.data.repository

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sumit.simplemobileaisuite.BuildConfig
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [GeminiRepository] using the Google Generative AI SDK.
 * API Key is securely fetched from BuildConfig via the Secrets Gradle Plugin.
 */
@Singleton
class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.6-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    override fun generateContentStream(prompt: String): Flow<String> = flow {
        generativeModel.generateContentStream(prompt).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }.catch { e ->
        throw Exception("Failed to generate content: ${e.localizedMessage}")
    }.flowOn(Dispatchers.IO)

    override fun generateContentWithImageStream(prompt: String, bitmap: Bitmap): Flow<String> =
        flow {
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            generativeModel.generateContentStream(inputContent).collect { chunk ->
                emit(chunk.text ?: "")
            }
        }.catch { e ->
            throw Exception("Failed to generate content with image: ${e.localizedMessage}")
        }.flowOn(Dispatchers.IO)
}
