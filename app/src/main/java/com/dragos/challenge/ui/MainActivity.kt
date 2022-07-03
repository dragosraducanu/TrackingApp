package com.dragos.challenge.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.dragos.challenge.service.LocationService
import com.dragos.challenge.service.ServiceConnectionManager
import com.dragos.challenge.ui.screen.MainScreen
import com.dragos.challenge.ui.theme.DragosChallengeTheme
import com.dragos.challenge.util.NetworkConnectivityManager
import com.dragos.challenge.util.hasLocationPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var networkConnectivityManager: NetworkConnectivityManager

    @Inject
    lateinit var serviceConnectionManager: ServiceConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DragosChallengeTheme {
                MainScreen(
                    state = viewModel.uiState.collectAsState(),
                    messageFlow = viewModel.message
                ) {
                    viewModel.toggleTrackingState()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                val active = it.isActive ?: return@collectLatest

                if (active) {
                    sendCommandToService(LocationService.LocationServiceAction.START)
                } else {
                    sendCommandToService(LocationService.LocationServiceAction.STOP)
                }
            }
        }

        lifecycleScope.launch {
            networkConnectivityManager.connectivityFlow.collectLatest {
                viewModel.syncOfflineData()
            }
        }

        lifecycleScope.launch {
            serviceConnectionManager.serviceFlow.collectLatest {
                when (it) {
                    is ServiceConnectionManager.CONNECTED ->
                        viewModel.setTrackingActive(it.isTracking)
                    else -> {
                        //no-op
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        serviceConnectionManager.bindService()
    }

    override fun onStop() {
        super.onStop()
        serviceConnectionManager.unbindService()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTrackingData()
    }


    private fun sendCommandToService(command: LocationService.LocationServiceAction) {
        if (command == LocationService.LocationServiceAction.START
            && !hasLocationPermission(this)) {
                performLocationPermissionRequest()
            return
        }

        startService(
            Intent(this, LocationService::class.java).apply {
                action = command.name
            }
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                // location granted
                sendCommandToService(LocationService.LocationServiceAction.START)
            }
            else -> {
                // no location granted
                Toast.makeText(
                    this@MainActivity,
                    "The location access permission has not been granted",
                    Toast.LENGTH_SHORT
                )
            }
        }
    }

    private fun performLocationPermissionRequest() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }
}