package com.google.aistudio.geminichat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.aistudio.geminichat.data.GeminiRepository
import com.google.aistudio.geminichat.data.SecurityManager
import com.google.aistudio.geminichat.data.model.ChatMessage
import com.google.aistudio.geminichat.data.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel orchestrating AI Chat state and business logic.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val securityManager = SecurityManager(application.applicationContext)
    private val repository = GeminiRepository(securityManager)

    private val _uiState = MutableStateFlow(
        ChatUiState(
            hasApiKey = securityManager.hasApiKey(),
            showApiKeyDialog = !securityManager.hasApiKey()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Add friendly welcome greeting if empty
        if (_uiState.value.messages.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    messages = listOf(
                        ChatMessage(
                            role = MessageRole.MODEL,
                            content = "Hello! I am Gemini. How can I help you today?"
                        )
                    )
                )
            }
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(currentInputText = newText) }
    }

    fun sendMessage() {
        val prompt = _uiState.value.currentInputText.trim()
        if (prompt.isBlank() || _uiState.value.isGenerating) return

        if (!securityManager.hasApiKey()) {
            _uiState.update { it.copy(showApiKeyDialog = true, errorMessage = "Please enter your Gemini API Key to chat.") }
            return
        }

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = prompt
        )

        val aiMessageId = UUID.randomUUID().toString()
        val aiPlaceholderMessage = ChatMessage(
            id = aiMessageId,
            role = MessageRole.MODEL,
            content = "",
            isStreaming = true
        )

        // Clear input field and append user & placeholder message
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + aiPlaceholderMessage,
                currentInputText = "",
                isGenerating = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val responseBuilder = StringBuilder()

            repository.sendMessageStream(prompt)
                .catch { exception ->
                    val errorText = "Error: ${exception.localizedMessage ?: "Unknown error occurred"}"
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == aiMessageId) {
                                    msg.copy(
                                        content = if (responseBuilder.isNotEmpty()) responseBuilder.toString() else errorText,
                                        isStreaming = false,
                                        isError = true
                                    )
                                } else msg
                            },
                            isGenerating = false,
                            errorMessage = exception.localizedMessage
                        )
                    }
                }
                .collect { chunk ->
                    responseBuilder.append(chunk)
                    val currentText = responseBuilder.toString()

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == aiMessageId) {
                                    msg.copy(content = currentText, isStreaming = true)
                                } else msg
                            }
                        )
                    }
                }

            // Mark streaming as complete
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(isStreaming = false)
                        } else msg
                    },
                    isGenerating = false
                )
            }
        }
    }

    fun saveApiKey(apiKey: String) {
        securityManager.saveApiKey(apiKey)
        repository.resetChatSession()
        _uiState.update {
            it.copy(
                hasApiKey = true,
                showApiKeyDialog = false,
                errorMessage = null
            )
        }
    }

    fun deleteApiKey() {
        securityManager.deleteApiKey()
        _uiState.update {
            it.copy(
                hasApiKey = false,
                showApiKeyDialog = true
            )
        }
    }

    fun openApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun dismissApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    fun openClearChatDialog() {
        _uiState.update { it.copy(showClearChatDialog = true) }
    }

    fun dismissClearChatDialog() {
        _uiState.update { it.copy(showClearChatDialog = false) }
    }

    fun clearChat() {
        repository.resetChatSession()
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        role = MessageRole.MODEL,
                        content = "Chat cleared. What would you like to explore next?"
                    )
                ),
                showClearChatDialog = false,
                isGenerating = false,
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
