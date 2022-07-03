package com.dragos.challenge.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ServiceConnectionManager(
    private val context: Context
): CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val _serviceFlow = MutableSharedFlow<ServiceConnectionState>()
    val serviceFlow = _serviceFlow.asSharedFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(comp: ComponentName?, binder: IBinder?) {
            val service = (binder as LocationService.LocationServiceBinder).service
            launch {
                _serviceFlow.emit(CONNECTED(service.isTracking))
            }
        }

        override fun onServiceDisconnected(comp: ComponentName?) {
            launch {
                _serviceFlow.emit(DISCONNECTED)
            }
        }
    }

    fun bindService() {
        Intent(context, LocationService::class.java).also {
            context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        Intent(context, LocationService::class.java).also {
            context.unbindService(serviceConnection)
        }
    }

    sealed class ServiceConnectionState
    data class CONNECTED(val isTracking: Boolean): ServiceConnectionState()
    object DISCONNECTED: ServiceConnectionState()
}