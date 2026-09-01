package com.sumit.simplemobileaisuite.di

import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import com.sumit.simplemobileaisuite.domain.repository.SmartChatRepository
import com.sumit.simplemobileaisuite.domain.usecase.DetectObjectsUseCase
import com.sumit.simplemobileaisuite.domain.usecase.GenerateSmartResponseUseCase
import com.sumit.simplemobileaisuite.domain.usecase.GetGeminiResponseUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt module to provide Use Case instances.
 * This keeps the Domain layer pure and free from DI annotations.
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGenerateSmartResponseUseCase(
        smartChatRepository: SmartChatRepository
    ): GenerateSmartResponseUseCase {
        return GenerateSmartResponseUseCase(smartChatRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetGeminiResponseUseCase(
        geminiRepository: GeminiRepository
    ): GetGeminiResponseUseCase {
        return GetGeminiResponseUseCase(geminiRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDetectObjectsUseCase(
        objectDetectorRepository: ObjectDetectorRepository
    ): DetectObjectsUseCase {
        return DetectObjectsUseCase(objectDetectorRepository)
    }
}
