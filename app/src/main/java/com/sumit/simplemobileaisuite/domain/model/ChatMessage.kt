package com.sumit.simplemobileaisuite.domain.model

import java.util.UUID

/**
 * Domain model representing a single chat message.
 * Shared across different AI chat features.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean
)
