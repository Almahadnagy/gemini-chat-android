package com.google.aistudio.geminichat.data.model

import java.util.UUID

enum class MessageRole {
    USER,
    MODEL,
    SYSTEM
}

/**
 * Immutable chat message state model.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)
