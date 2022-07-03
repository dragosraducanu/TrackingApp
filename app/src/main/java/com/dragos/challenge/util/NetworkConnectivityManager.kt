package com.dragos.challenge.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class NetworkConnectivityManager(
    context: Context,
): CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val _connectivityFlow = MutableSharedFlow<NetworkConnectionState>()
    val connectivityFlow = _connectivityFlow.asSharedFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            launch {
                _connectivityFlow.emit(NetworkConnectionState.CONNECTED)
            }

        }

        override fun onUnavailable() {
            super.onUnavailable()

            launch {
                _connectivityFlow.emit(NetworkConnectionState.DISCONNECTED)
            }
        }
    }

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    enum class NetworkConnectionState {
        CONNECTED,
        DISCONNECTED
    }
}