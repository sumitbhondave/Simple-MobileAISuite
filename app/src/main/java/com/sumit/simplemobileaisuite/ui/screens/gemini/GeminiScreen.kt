package com.sumit.simplemobileaisuite.ui.screens.gemini

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sumit.simplemobileaisuite.R
import com.sumit.simplemobileaisuite.ui.components.ImagePickerSection

/**
 * Screen for interacting with Gemini AI.
 * Displays the AI response and provides an input section for text and images.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: GeminiViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Smart Auto-Scroll: Lock to the bottom item safely
    LaunchedEffect(uiState.responseText) {
        if (uiState.responseText.isNotEmpty()) {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                listState.scrollToItem(totalItems - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.gemini_title),
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
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // Use navigationBarsPadding and imePadding to ensure visibility and keyboard compatibility
            ImagePickerSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(8.dp),
                onSendPrompt = { prompt, bitmap ->
                    if (bitmap != null) {
                        viewModel.askQuestionWithImage(prompt, bitmap)
                    } else {
                        viewModel.askQuestion(prompt)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Response Display Area
            GeminiResponseArea(
                modifier = Modifier.weight(1f),
                responseText = uiState.responseText,
                listState = listState
            )
        }
    }
}

@Composable
fun GeminiResponseArea(
    modifier: Modifier = Modifier,
    responseText: String,
    listState: LazyListState
) {
    SelectionContainer(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = responseText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // Dedicated scroll target
            item {
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}
