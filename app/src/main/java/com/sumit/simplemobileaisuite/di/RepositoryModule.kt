package com.sumit.simplemobileaisuite.di

import com.sumit.simplemobileaisuite.data.repository.GeminiRepositoryImpl
import com.sumit.simplemobileaisuite.data.repository.NetworkMonitorImpl
import com.sumit.simplemobileaisuite.data.repository.ObjectDetectorRepositoryImpl
import com.sumit.simplemobileaisuite.data.repository.OfflineChatRepositoryImpl
import com.sumit.simplemobileaisuite.data.repository.SmartChatRepositoryImpl
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import com.sumit.simplemobileaisuite.domain.repository.NetworkMonitor
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import com.sumit.simplemobileaisuite.domain.repository.SmartChatRepository
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

    @Binds
    @Singleton
    abstract fun bindOfflineChatRepository(
        offlineChatRepositoryImpl: OfflineChatRepositoryImpl
    ): OfflineChatRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        networkMonitorImpl: NetworkMonitorImpl
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindSmartChatRepository(
        smartChatRepositoryImpl: SmartChatRepositoryImpl
    ): SmartChatRepository
}
