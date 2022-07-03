package com.dragos.challenge.data.source.db

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class TrackingDataStore(private val context: Context) : TrackingDataStoreContract {
    override fun fetchTrackingData(): Flow<TrackingData> {
        return context.dataStore.data.catch { exception ->
            exception.printStackTrace()
        }
    }

    override suspend fun saveLocation(location: Location) {
        context.dataStore.updateData { data ->
            data.toBuilder().addLocation(location).build()
        }
    }

    override suspend fun updateLocations(locations: List<Location>) {

        context.dataStore.updateData { data ->
            val dataBuilder = data.toBuilder()

            locations.forEach { updatedLocation ->
                val existing = data.locationList.find { originalLocation ->
                    updatedLocation.lat == originalLocation.lat
                            && updatedLocation.lng == originalLocation.lng
                }

                if (existing != null) {
                    dataBuilder.setLocation(
                        data.locationList.indexOf(existing), existing.toBuilder()
                            .setImageUri(updatedLocation.imageUri)
                            .setImageUrl(updatedLocation.imageUrl)
                            .build()
                    )
                }
            }


            dataBuilder.build()
        }
    }

    override suspend fun removeLocations(locations: List<Location>) {
        context.dataStore.updateData { data ->
            val dataBuilder = data.toBuilder()

            locations.forEach { loc ->
                val index = dataBuilder.locationList.indexOf(loc)
                if (index != -1) {
                    dataBuilder.removeLocation(index)
                }
            }

            dataBuilder.build()
        }
    }

    override suspend fun removeAll() {
        context.dataStore.updateData { data ->
            data.toBuilder().clear().build()
        }
    }
}