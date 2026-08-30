package com.sumit.simplemobileaisuite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sumit.simplemobileaisuite.ui.screens.detector.ObjectDetectionScreen
import com.sumit.simplemobileaisuite.ui.screens.detector.ObjectDetectionViewModel
import com.sumit.simplemobileaisuite.ui.screens.gemini.GeminiScreen
import com.sumit.simplemobileaisuite.ui.screens.gemini.GeminiViewModel
import com.sumit.simplemobileaisuite.ui.screens.home.HomeScreen
import com.sumit.simplemobileaisuite.ui.screens.offline_chat.OfflineChatScreen
import com.sumit.simplemobileaisuite.ui.screens.offline_chat.OfflineChatViewModel

/**
 * Main navigation graph for the application.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGemini = { navController.navigate(Screen.Gemini.route) },
                onNavigateToDetector = { navController.navigate(Screen.Detector.route) },
                onNavigateToOfflineChat = { navController.navigate(Screen.OfflineChat.route) }
            )
        }

        composable(Screen.Gemini.route) {
            val viewModel: GeminiViewModel = hiltViewModel()
            GeminiScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Detector.route) {
            val viewModel: ObjectDetectionViewModel = hiltViewModel()
            ObjectDetectionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OfflineChat.route) {
            val viewModel: OfflineChatViewModel = hiltViewModel()
            OfflineChatScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
