package com.sumit.simplemobileaisuite.ui.screens.gemini

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumit.simplemobileaisuite.ui.components.ImagePickerSection

/**
 * Screen for interacting with Gemini AI.
 * Displays the AI response and provides an input section for text and images.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiScreen(
    viewModel: GeminiViewModel,
    onNavigateBack: () -> Unit,
) {
    val responseText by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini AI Chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Response Display Area
            GeminiResponseArea(
                responseText = responseText,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Section (Text + Image)
            ImagePickerSection(
                onSendPrompt = { prompt, bitmap ->
                    if (bitmap != null) {
                        viewModel.askQuestionWithImage(prompt, bitmap)
                    } else {
                        viewModel.askQuestion(prompt)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GeminiResponseArea(
    responseText: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading && responseText.isEmpty()) {
            CircularProgressIndicator()
        }
        Text(
            text = responseText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
