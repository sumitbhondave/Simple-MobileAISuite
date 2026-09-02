package com.sumit.simplemobileaisuite.ui.screens.offline_chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.domain.model.ChatMessage
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus

/**
 * Screen for offline AI chat using the Gemma model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineChatScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: OfflineChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val lastMessageText = uiState.messages.lastOrNull()?.text ?: ""

    // Smart Auto-Scroll: Lock to the bottom item (anchor) whenever text changes
    LaunchedEffect(uiState.messages.size, lastMessageText) {
        val totalItems = listState.layoutInfo.totalItemsCount
        if (totalItems > 0) {
            listState.scrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.offline_chat_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // MODERN INPUT FIELD in bottomBar slot for edge-to-edge feel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(8.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isModelReady = uiState.offlineLLMStatus is OfflineLLMStatus.Ready

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        placeholder = { Text(stringResource(R.string.offline_chat_placeholder)) },
                        enabled = !uiState.isGenerating && isModelReady,
                        maxLines = 5,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        )
                    )

                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = inputText.isNotBlank() && !uiState.isGenerating && isModelReady,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.38f
                            )
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState.offlineLLMStatus) {
                is OfflineLLMStatus.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.loading_gemma),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is OfflineLLMStatus.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // 1. Chat History List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                        ) {
                            items(
                                items = uiState.messages,
                                key = { it.id }
                            ) { message ->
                                ChatBubble(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    message = message
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            // Bottom Anchor
                            item(key = "bottom_anchor") {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    modifier: Modifier = Modifier,
    message: ChatMessage
) {
    if (message.text.isEmpty()) return

    val backgroundColor = if (message.isFromUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (message.isFromUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = backgroundColor,
            tonalElevation = if (message.isFromUser) 0.dp else 1.dp,
            modifier = if (message.isFromUser) {
                Modifier.widthIn(max = 300.dp)
            } else {
                Modifier.fillMaxWidth()
            }
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
