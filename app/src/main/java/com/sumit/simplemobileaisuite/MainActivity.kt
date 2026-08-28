package com.sumit.simplemobileaisuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sumit.simplemobileaisuite.ui.navigation.AppNavigation
import com.sumit.simplemobileaisuite.ui.theme.SimpleMobileAISuiteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity of the application.
 * Annotated with @AndroidEntryPoint to enable Hilt injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleMobileAISuiteTheme {
                // Main entry point for navigation
                AppNavigation()
            }
        }
    }
}
