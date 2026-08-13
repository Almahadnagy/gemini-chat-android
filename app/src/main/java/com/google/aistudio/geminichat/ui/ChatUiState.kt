package com.google.aistudio.geminichat.ui

import com.google.aistudio.geminichat.data.model.ChatMessage

/**
 * Unidirectional Data Flow state model for the Chat Screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val hasApiKey: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val showClearChatDialog: Boolean = false,
    val errorMessage: String? = null,
    val currentInputText: String = ""
)
