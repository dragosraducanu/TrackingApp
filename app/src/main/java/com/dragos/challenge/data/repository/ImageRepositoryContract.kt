package com.dragos.challenge.data.repository

import com.dragos.challenge.data.source.db.Location
import com.dragos.challenge.data.source.db.TrackingData
import kotlinx.coroutines.flow.Flow

interface ImageRepositoryContract {
    suspend fun fetchTrackingData(): Flow<TrackingData>
    suspend fun removeAllImages()
    suspend fun fetchImageForLatLng(latitude: Double, longitude: Double): Location?
    suspend fun saveLocation(location: Location)
    suspend fun updateLocations(locations: List<Location>)
    suspend fun removeLocations(locations: List<Location>)
}