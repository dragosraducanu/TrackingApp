package com.dragos.challenge.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.dragos.challenge.R
import com.dragos.challenge.data.repository.ImageRepository
import com.dragos.challenge.ui.MainActivity
import com.dragos.challenge.util.hasLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : LifecycleService() {

    @Inject
    protected lateinit var imageRepository: ImageRepository

    @Inject
    protected lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val binder: LocationServiceBinder = LocationServiceBinder()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            result.locations.forEach {
                lifecycleScope.launch {
                    imageRepository.fetchImageForLatLng(it.latitude, it.longitude)?.let {
                        imageRepository.saveLocation(it)
                    }
                }
            }
        }
    }

    var isTracking = false

    inner class LocationServiceBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action?.toLocationServiceAction() ?: return super.onStartCommand(
            intent,
            flags,
            startId
        )

        when (action) {
            LocationServiceAction.START -> {
                if (!isTracking) {
                    startService()
                }
            }
            LocationServiceAction.STOP -> {
                stopService()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager) {
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIF_CHANNEL_ID,
                NOTIF_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    private fun stopService() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        stopForeground(true)
        isTracking = false
    }

    @SuppressLint("MissingPermission")
    private fun startService() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(manager)
        }

        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_footprint)
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(pendingIntent)

        val notification = builder.build()

        manager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)

        val locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_MS
            fastestInterval = FASTEST_INTERVAL
            priority = PRIORITY_HIGH_ACCURACY
            smallestDisplacement = SMALLEST_DISPLACEMENT
        }

        if (hasLocationPermission(this)) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            isTracking = true
        }

    }

    private fun String.toLocationServiceAction() = LocationServiceAction.valueOf(this)

    enum class LocationServiceAction {
        START,
        STOP
    }

    companion object {
        private const val NOTIFICATION_ID = 0x0001
        private const val NOTIF_CHANNEL_ID = "notif_channel_tracking"
        private const val NOTIF_CHANNEL_NAME = "Active activity notifications"

        private const val UPDATE_INTERVAL_MS = 1000L
        private const val FASTEST_INTERVAL = 1000L
        private const val SMALLEST_DISPLACEMENT = 100f
    }
}