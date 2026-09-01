package com.sumit.simplemobileaisuite.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.sumit.simplemobileaisuite.domain.repository.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkMonitor {

    override fun isOnline(): Boolean {
        // 1. Get the system connectivity service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 2. Get the current active network
        val network = connectivityManager.activeNetwork ?: return false

        // 3. Get the capabilities of that network
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // 4. Verify it has internet AND the system has validated the connection
        // (This prevents returning true when stuck on a hotel/airport login screen)
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}