package com.sumit.simplemobileaisuite.domain.model

/**
 * Domain entity representing various outcomes of the Hybrid AI logic.
 */
sealed class SmartResponse {
    data class TextChunk(val text: String) : SmartResponse()

    sealed class SystemEvent : SmartResponse() {
        object LocalAiNotInitialized : SystemEvent()
        data class LocalAiError(val errorMessage: String) : SystemEvent()
        object CloudFallbackStarted : SystemEvent()
        data class CloudError(val errorMessage: String) : SystemEvent()
        object HardwareUnsupported : SystemEvent()
        object OfflineNoLocalFallback : SystemEvent()
    }
}
