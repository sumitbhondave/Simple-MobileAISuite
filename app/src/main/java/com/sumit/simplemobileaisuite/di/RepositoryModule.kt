package com.sumit.simplemobileaisuite.di

import com.sumit.simplemobileaisuite.data.repository.GeminiRepositoryImpl
import com.sumit.simplemobileaisuite.data.repository.ObjectDetectorRepositoryImpl
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide repository implementations.
 * Follows the Dependency Inversion Principle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGeminiRepository(
        geminiRepositoryImpl: GeminiRepositoryImpl
    ): GeminiRepository

    @Binds
    @Singleton
    abstract fun bindObjectDetectorRepository(
        objectDetectorRepositoryImpl: ObjectDetectorRepositoryImpl
    ): ObjectDetectorRepository
}
