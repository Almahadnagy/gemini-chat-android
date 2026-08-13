package com.google.aistudio.geminichat.data

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Data Repository for interacting with Google GenAI SDK.
 * Manages ChatSession lifecycle and emits streaming response chunks.
 */
class GeminiRepository(private val securityManager: SecurityManager) {

    private var generativeModel: GenerativeModel? = null
    private var chatSession: Chat? = null
    private var currentApiKey: String? = null

    /**
     * Initializes or updates the GenerativeModel when API key changes.
     */
    private fun ensureModelInitialized() {
        val apiKey = securityManager.getApiKey()
            ?: throw IllegalStateException("Gemini API Key is missing. Please set it in Settings.")

        if (generativeModel == null || currentApiKey != apiKey) {
            currentApiKey = apiKey
            generativeModel = GenerativeModel(
                modelName = MODEL_FLASH,
                apiKey = apiKey
            )
            chatSession = generativeModel?.startChat()
        }
    }

    /**
     * Resets the active multi-turn conversation session.
     */
    fun resetChatSession() {
        val apiKey = securityManager.getApiKey() ?: return
        generativeModel = GenerativeModel(
            modelName = MODEL_FLASH,
            apiKey = apiKey
        )
        chatSession = generativeModel?.startChat()
    }

    /**
     * Sends user message and yields token chunks in real-time via Kotlin Flow.
     */
    fun sendMessageStream(userPrompt: String): Flow<String> = flow {
        ensureModelInitialized()
        val session = chatSession ?: throw IllegalStateException("Failed to initialize Gemini Chat session.")

        // Stream tokens as they arrive from Gemini 1.5 Flash
        session.sendMessageStream(userPrompt).collect { chunk: GenerateContentResponse ->
            chunk.text?.let { textChunk ->
                emit(textChunk)
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        const val MODEL_FLASH = "gemini-1.5-flash"
    }
}
