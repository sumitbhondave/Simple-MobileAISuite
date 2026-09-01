package com.sumit.simplemobileaisuite.domain.repository

import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for orchestrated Smart Chat logic.
 */
interface SmartChatRepository {
    fun getSmartResponse(prompt: String): Flow<SmartResponse>
}
