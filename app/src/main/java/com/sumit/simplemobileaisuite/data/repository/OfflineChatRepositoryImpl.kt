package com.sumit.simplemobileaisuite.data.repository

import com.sumit.simplemobileaisuite.data.datasource.local.LLMInferenceHelper
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [OfflineChatRepository] using [LLMInferenceHelper].
 */
@Singleton
class OfflineChatRepositoryImpl @Inject constructor(
    private val llmHelper: LLMInferenceHelper
) : OfflineChatRepository {

    override val partialResults: SharedFlow<Pair<String, Boolean>> = llmHelper.partialResults
    
    override val offlineLLMStatus: StateFlow<OfflineLLMStatus> = llmHelper.offlineLLMStatus

    override fun initialize() {
        llmHelper.initializeLLM()
    }

    override fun generateResponse(prompt: String) {
        llmHelper.generateResponseAsync(prompt)
    }

    override fun close() {
        llmHelper.close()
    }
}
