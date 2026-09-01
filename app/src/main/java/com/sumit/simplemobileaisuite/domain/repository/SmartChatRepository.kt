package com.sumit.simplemobileaisuite.domain.repository

import com.sumit.simplemobileaisuite.domain.model.SmartResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for orchestrated Smart Chat logic.
 * Handles the decision-making process for routing requests between local (Edge) 
 * and remote (Cloud) AI models based on hardware readiness and connectivity.
 */
interface SmartChatRepository {
    /**
     * Generates a response for the given prompt using a hybrid strategy.
     * @param prompt The user's input string.
     * @return A [Flow] of [SmartResponse] objects containing text chunks or system events.
     */
    fun getSmartResponse(prompt: String): Flow<SmartResponse>
}
