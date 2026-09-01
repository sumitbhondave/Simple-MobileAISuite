package com.sumit.simplemobileaisuite.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumit.simplemobileaisuite.R

/**
 * Dashboard screen providing entry points to all AI features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToGemini: () -> Unit,
    onNavigateToDetector: () -> Unit,
    onNavigateToOfflineChat: () -> Unit,
    onNavigateToSmartChat: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeHeaderContent()

            FeatureCard(
                title = stringResource(R.string.feature_gemini_title),
                description = stringResource(R.string.feature_gemini_desc),
                icon = Icons.Default.AutoAwesome,
                onClick = onNavigateToGemini
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = stringResource(R.string.feature_detector_title),
                description = stringResource(R.string.feature_detector_desc),
                icon = Icons.Default.CameraAlt,
                onClick = onNavigateToDetector
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = stringResource(R.string.feature_offline_chat_title),
                description = stringResource(R.string.feature_offline_chat_desc),
                icon = Icons.Default.ChatBubble,
                onClick = onNavigateToOfflineChat
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                title = stringResource(R.string.feature_smart_chat_title),
                description = stringResource(R.string.feature_smart_chat_desc),
                icon = Icons.Default.Psychology,
                onClick = onNavigateToSmartChat
            )
        }
    }
}

@Composable
fun HomeHeaderContent(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.home_header),
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(bottom = 32.dp)
    )
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
