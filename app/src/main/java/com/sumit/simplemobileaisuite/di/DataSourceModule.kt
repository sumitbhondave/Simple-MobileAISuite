package com.sumit.simplemobileaisuite.di

import com.sumit.simplemobileaisuite.data.datasource.local.LLMInferenceHelper
import com.sumit.simplemobileaisuite.data.datasource.local.MediaPipeObjectDetectorDataSource
import com.sumit.simplemobileaisuite.data.datasource.local.ObjectDetectorDataSource
import com.sumit.simplemobileaisuite.data.datasource.local.OfflineLLMDataSource
import com.sumit.simplemobileaisuite.data.datasource.remote.GeminiRemoteDataSource
import com.sumit.simplemobileaisuite.data.datasource.remote.GoogleAiGeminiRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindObjectDetectorDataSource(
        impl: MediaPipeObjectDetectorDataSource
    ): ObjectDetectorDataSource

    @Binds
    @Singleton
    abstract fun bindOfflineLLMDataSource(
        impl: LLMInferenceHelper
    ): OfflineLLMDataSource

    @Binds
    @Singleton
    abstract fun bindGeminiRemoteDataSource(
        impl: GoogleAiGeminiRemoteDataSource
    ): GeminiRemoteDataSource
}
