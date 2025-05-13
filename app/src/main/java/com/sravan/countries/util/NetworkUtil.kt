package com.sravan.countries.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Utility class to check network connectivity status
 */
object NetworkUtil {

    /**
     * Checks if the device is currently connected to a network
     *
     * @param context Application context
     * @return true if connected to WiFi or cellular, false otherwise
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkCapabilities != null && (
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    /**
     * Provides a Flow of network status changes
     *
     * @param context Application context
     * @return Flow emitting true when connected, false when disconnected
     */
    fun observeNetworkStatus(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        // Initial value
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected =
            networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))

        trySend(isConnected)

        // Register for network changes
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Cleanup
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.conflate()
} 