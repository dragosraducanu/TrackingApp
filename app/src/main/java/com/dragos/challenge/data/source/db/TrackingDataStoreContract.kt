package com.dragos.challenge.data.source.db

import kotlinx.coroutines.flow.Flow

interface TrackingDataStoreContract {
    fun fetchTrackingData(): Flow<TrackingData>
    suspend fun removeAll()
    suspend fun saveLocation(location: Location)
    suspend fun updateLocations(locations: List<Location>)
    suspend fun removeLocations(locations: List<Location>)
}