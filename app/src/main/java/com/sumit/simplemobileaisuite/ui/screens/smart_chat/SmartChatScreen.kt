package com.sumit.simplemobileaisuite.ui.screens.smart_chat

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
import androidx.compose.ui.unit.dp
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.domain.model.ChatMessage
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus

/**
 * Screen for hybrid "Smart Chat" (Edge-first, Cloud-fallback).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartChatScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: SmartChatViewModel
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
            // Scroll to the absolute last item (the bottom_anchor)
            listState.scrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.smart_chat_title),
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

                // Edge AI Status Banner
                val (bannerColor, statusText) = when {
                    uiState.headerStatusMessage != null -> {
                        val color = if (uiState.headerStatusMessage!!.contains("Error") ||
                            uiState.headerStatusMessage!!.contains("Unavailable")
                        ) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                        color to uiState.headerStatusMessage!!
                    }

                    uiState.offlineLLMStatus is OfflineLLMStatus.Ready ->
                        MaterialTheme.colorScheme.primaryContainer to stringResource(R.string.edge_ai_ready)

                    uiState.offlineLLMStatus is OfflineLLMStatus.Loading ->
                        MaterialTheme.colorScheme.secondaryContainer to stringResource(R.string.warming_up_edge_ai)

                    uiState.offlineLLMStatus is OfflineLLMStatus.Error ->
                        MaterialTheme.colorScheme.errorContainer to stringResource(R.string.edge_ai_unavailable)

                    else -> MaterialTheme.colorScheme.surfaceVariant to stringResource(R.string.initializing_edge_ai)
                }

                Surface(
                    color = bannerColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.offlineLLMStatus is OfflineLLMStatus.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
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
                    val isModelLoading = uiState.offlineLLMStatus is OfflineLLMStatus.Loading

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        placeholder = {
                            Text(
                                if (isModelLoading) stringResource(R.string.warming_up_edge_ai)
                                else stringResource(R.string.smart_chat_placeholder)
                            )
                        },
                        enabled = !uiState.isGenerating && !isModelLoading,
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
                        enabled = inputText.isNotBlank() && !uiState.isGenerating && !isModelLoading,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Chat History List
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        SmartChatBubble(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            message = message
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    // Bottom Anchor item
                    item(key = "bottom_anchor") {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SmartChatBubble(
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
