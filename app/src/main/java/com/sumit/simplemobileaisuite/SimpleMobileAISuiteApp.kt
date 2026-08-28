package com.sumit.simplemobileaisuite

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class required for Hilt dependency injection.
 * The @HiltAndroidApp annotation triggers Hilt's code generation.
 */
@HiltAndroidApp
class SimpleMobileAISuiteApp : Application()
