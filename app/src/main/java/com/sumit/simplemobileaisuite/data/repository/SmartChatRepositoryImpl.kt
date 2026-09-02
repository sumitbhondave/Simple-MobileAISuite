package com.sumit.simplemobileaisuite.data.repository

import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import com.sumit.simplemobileaisuite.domain.repository.GeminiRepository
import com.sumit.simplemobileaisuite.domain.repository.NetworkMonitor
import com.sumit.simplemobileaisuite.domain.repository.OfflineChatRepository
import com.sumit.simplemobileaisuite.domain.repository.SmartChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformWhile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartChatRepositoryImpl @Inject constructor(
    private val offlineChatRepository: OfflineChatRepository,
    private val geminiRepository: GeminiRepository,
    private val networkMonitor: NetworkMonitor
) : SmartChatRepository {

    override fun getSmartResponse(prompt: String): Flow<SmartResponse> = flow {
        val isLocalReady = offlineChatRepository.offlineLLMStatus.value is OfflineLLMStatus.Ready

        if (isLocalReady) {
            try {
                offlineChatRepository.generateResponse(prompt)

                emitAll(
                    offlineChatRepository.partialResults.transformWhile { (chunk, isDone) ->
                        if (chunk.contains("[Hardware Error")) {
                            throw IllegalStateException("Hardware rejected OpenCL shader.")
                        }
                        emit(SmartResponse.TextChunk(chunk))
                        !isDone
                    }
                )
                return@flow
            } catch (e: Exception) {
                emit(SmartResponse.SystemEvent.LocalAiError(e.message ?: "Unknown"))
            }
        } else {
            emit(SmartResponse.SystemEvent.LocalAiNotInitialized)
        }

        // Cloud Fallback
        if (!networkMonitor.isOnline()) {
            emit(SmartResponse.SystemEvent.OfflineNoLocalFallback)
            return@flow
        }

        try {
            geminiRepository.generateContentStream(prompt).collect { cloudChunk ->
                emit(SmartResponse.TextChunk(cloudChunk))
            }
        } catch (cloudError: Exception) {
            emit(SmartResponse.SystemEvent.CloudError(cloudError.localizedMessage ?: "API Error"))
        }
    }
}
