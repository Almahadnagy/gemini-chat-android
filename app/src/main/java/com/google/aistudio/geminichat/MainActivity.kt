package com.google.aistudio.geminichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.aistudio.geminichat.ui.ChatViewModel
import com.google.aistudio.geminichat.ui.screens.ChatScreen
import com.google.aistudio.geminichat.ui.theme.GeminiChatTheme

/**
 * Principal Activity entry point for Gemini Chat Studio.
 * Implements Android 15 Edge-to-Edge design guidelines.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable full Edge-to-Edge display with transparent system bars
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            GeminiChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}
