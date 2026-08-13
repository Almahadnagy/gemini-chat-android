package com.google.aistudio.geminichat.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.aistudio.geminichat.ui.ChatViewModel
import com.google.aistudio.geminichat.ui.components.ApiKeyDialog
import com.google.aistudio.geminichat.ui.components.ChatInputBar
import com.google.aistudio.geminichat.ui.components.ChatMessageItem
import com.google.aistudio.geminichat.ui.components.ClearChatDialog
import kotlinx.coroutines.launch

/**
 * Single-screen Material 3 Chat UI with real-time streaming and Unidirectional Data Flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Auto-scroll to the bottom whenever a new message is appended or streaming updates
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Display transient errors via Material 3 Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Gemini Chat",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                actions = {
                    // Clear Chat action button
                    IconButton(
                        onClick = { viewModel.openClearChatDialog() },
                        enabled = uiState.messages.isNotEmpty() && !uiState.isGenerating
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat"
                        )
                    }
                    // Key Settings action button
                    IconButton(onClick = { viewModel.openApiKeyDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                text = uiState.currentInputText,
                onTextChanged = viewModel::onInputTextChanged,
                onSendClicked = viewModel::sendMessage,
                isGenerating = uiState.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    ChatMessageItem(
                        message = message,
                        onCopyClicked = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard")
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (uiState.showApiKeyDialog) {
        ApiKeyDialog(
            hasExistingKey = uiState.hasApiKey,
            onDismiss = viewModel::dismissApiKeyDialog,
            onSaveKey = viewModel::saveApiKey,
            onDeleteKey = viewModel::deleteApiKey
        )
    }

    if (uiState.showClearChatDialog) {
        ClearChatDialog(
            onDismiss = viewModel::dismissClearChatDialog,
            onConfirm = viewModel::clearChat
        )
    }
}
