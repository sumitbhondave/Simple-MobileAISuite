package com.sumit.simplemobileaisuite.ui.navigation

/**
 * Sealed class representing different screens in the application.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Gemini : Screen("gemini")
    object Detector : Screen("detector")
}
