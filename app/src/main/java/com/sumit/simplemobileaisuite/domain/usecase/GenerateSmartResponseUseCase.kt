package com.sumit.simplemobileaisuite.domain.usecase

import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import com.sumit.simplemobileaisuite.domain.repository.SmartChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Pure Use Case that delegates to the SmartChatRepository.
 * Follows Clean Architecture by remaining free of Android dependencies and UI formatting.
 */
class GenerateSmartResponseUseCase(
    private val repository: SmartChatRepository
) {
    operator fun invoke(prompt: String): Flow<SmartResponse> {
        return repository.getSmartResponse(prompt)
    }
}
