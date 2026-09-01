package com.sumit.simplemobileaisuite.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NetworkMonitorImplTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkMonitor: NetworkMonitorImpl

    @Before
    fun setup() {
        context = mockk()
        connectivityManager = mockk()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        networkMonitor = NetworkMonitorImpl(context)
    }

    @Test
    fun `isOnline should return true when network is connected and validated`() {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()

        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        assertTrue(networkMonitor.isOnline())
    }

    @Test
    fun `isOnline should return false when no active network`() {
        every { connectivityManager.activeNetwork } returns null
        assertFalse(networkMonitor.isOnline())
    }

    @Test
    fun `isOnline should return false when no capabilities`() {
        val network = mockk<Network>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null
        assertFalse(networkMonitor.isOnline())
    }

    @Test
    fun `isOnline should return false when not validated`() {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()

        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false

        assertFalse(networkMonitor.isOnline())
    }
}
