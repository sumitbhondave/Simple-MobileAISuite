package com.sumit.simplemobileaisuite.data.datasource.local

import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for offline LLM data source.
 */
interface OfflineLLMDataSource {
    val partialResults: SharedFlow<Pair<String, Boolean>>
    val offlineLLMStatus: StateFlow<OfflineLLMStatus>

    fun initialize()
    fun generateResponse(prompt: String)
    fun close()
}
