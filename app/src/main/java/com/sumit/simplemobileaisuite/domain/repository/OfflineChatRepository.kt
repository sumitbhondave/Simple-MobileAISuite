package com.sumit.simplemobileaisuite.domain.repository

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface defining the capabilities for offline AI chat using local LLM.
 * Follows the Dependency Inversion Principle.
 */
interface OfflineChatRepository {
    /**
     * SharedFlow that emits partial results from the LLM.
     * The Boolean indicates if the generation is complete.
     */
    val partialResults: SharedFlow<Pair<String, Boolean>>

    /**
     * Triggers the generation of a response for the given prompt.
     */
    fun generateResponse(prompt: String)

    /**
     * Closes the underlying LLM resources.
     */
    fun close()
}
