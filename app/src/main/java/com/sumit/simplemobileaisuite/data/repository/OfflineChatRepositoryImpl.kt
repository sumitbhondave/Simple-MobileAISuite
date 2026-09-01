package com.sumit.simplemobileaisuite.data.repository

import com.sumit.simplemobileaisuite.data.datasource.local.OfflineLLMDataSource
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [OfflineChatRepository] that delegates to [OfflineLLMDataSource].
 */
@Singleton
class OfflineChatRepositoryImpl @Inject constructor(
    private val localDataSource: OfflineLLMDataSource
) : OfflineChatRepository {

    override val partialResults: SharedFlow<Pair<String, Boolean>> = localDataSource.partialResults

    override val offlineLLMStatus: StateFlow<OfflineLLMStatus> = localDataSource.offlineLLMStatus

    override fun initialize() {
        localDataSource.initialize()
    }

    override fun generateResponse(prompt: String) {
        localDataSource.generateResponse(prompt)
    }

    override fun close() {
        localDataSource.close()
    }
}
