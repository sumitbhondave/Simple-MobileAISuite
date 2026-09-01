package com.sumit.simplemobileaisuite.data.repository

import android.graphics.Bitmap
import com.sumit.simplemobileaisuite.data.datasource.remote.GeminiRemoteDataSource
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [GeminiRepository] that delegates to [GeminiRemoteDataSource].
 */
@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeminiRemoteDataSource
) : GeminiRepository {

    override fun generateContentStream(prompt: String): Flow<String> {
        return remoteDataSource.generateContentStream(prompt)
    }

    override fun generateContentWithImageStream(prompt: String, bitmap: Bitmap): Flow<String> {
        return remoteDataSource.generateContentWithImageStream(prompt, bitmap)
    }
}
