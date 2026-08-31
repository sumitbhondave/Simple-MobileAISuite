package com.sumit.simplemobileaisuite.domain.model

/**
 * Represents the status of the offline LLM engine (Gemma).
 */
sealed class OfflineLLMStatus {
    object Idle : OfflineLLMStatus()
    object Loading : OfflineLLMStatus()
    object Ready : OfflineLLMStatus()
    data class Error(val message: String) : OfflineLLMStatus()
}
