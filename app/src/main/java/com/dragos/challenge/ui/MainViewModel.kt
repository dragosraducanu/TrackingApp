package com.dragos.challenge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dragos.challenge.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(null, emptyList()))
    val uiState: StateFlow<MainUiState> = _uiState

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    fun fetchTrackingData() {
        viewModelScope.launch {
            syncOfflineData()
            repository.fetchTrackingData()
                .collectLatest {
                    _uiState.emit(
                        _uiState.value.copy(
                            images = it.locationList.sortedBy { loc -> loc.timestamp }
                                .map { loc ->
                                    loc.imageUri
                                }
                        )
                    )
                }
        }
    }

    /**
     * Will try to fetch images for the locations that didn't manage to download yet
     */
    fun syncOfflineData() {
        viewModelScope.launch {
            val data = repository.fetchTrackingData().first().locationList

            data.forEach { loc ->
                if (loc.imageUri.isNullOrEmpty()) {
                    try {
                        val result = repository.fetchImageForLatLng(loc.lat, loc.lng)

                        if (result == null) {
                            repository.removeLocations(listOf(loc))
                        } else if (result.imageUri.isNotEmpty()) {
                            repository.updateLocations(listOf(result))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun setTrackingActive(active: Boolean) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(isActive = active))
        }
    }

    fun toggleTrackingState() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(isActive = _uiState.value.isActive?.not()))

            if (_uiState.value.isActive == true) {
                clearData()
            }
        }
    }

    private fun clearData() {
        viewModelScope.launch {
            repository.removeAllImages()
        }
    }
}