package com.dragos.challenge.data.repository

import com.dragos.challenge.data.source.api.FlickrService
import com.dragos.challenge.data.source.db.CacheLocation
import com.dragos.challenge.data.source.db.Location
import com.dragos.challenge.data.source.db.TrackingData
import com.dragos.challenge.data.source.db.TrackingDataStoreContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class ImageRepository @Inject constructor(
    private val flickrService: FlickrService,
    private val dataStore: TrackingDataStoreContract,
    private val cache: CacheLocation
) : ImageRepositoryContract {

    override suspend fun fetchImageForLatLng(latitude: Double, longitude: Double): Location? {
        val locationBuilder = Location.newBuilder().apply {
            lat = latitude
            lng = longitude
            timestamp = System.currentTimeMillis()
        }

        try {
            val searchResult = search(latitude, longitude).result.images
            if (searchResult.isEmpty()) return null

            val trackingData = fetchTrackingData().first().locationList

            val url = searchResult.find { image ->
                image.url != null &&
                        trackingData.none { location -> location.imageUrl == image.url }
            }?.url

            if (url != null) {
                locationBuilder.imageUrl = url

                val uri = preloadImage(url)

                if (uri != null) {
                    locationBuilder.imageUri = uri
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return locationBuilder.build()
    }

    override suspend fun saveLocation(location: Location) {
        dataStore.saveLocation(location)
    }

    override suspend fun updateLocations(locations: List<Location>) {
        dataStore.updateLocations(locations)
    }

    override suspend fun removeLocations(locations: List<Location>) {
        dataStore.removeLocations(locations)
    }

    override suspend fun fetchTrackingData(): Flow<TrackingData> =
        dataStore.fetchTrackingData()

    override suspend fun removeAllImages() {
        withContext(Dispatchers.IO) {
            cache.cacheDir.listFiles()?.forEach {
                it.delete()
            }
        }
        dataStore.removeAll()
    }

    private suspend fun preloadImage(url: String?): String? {
        val safeUrl = url ?: return null
        val fileName = File(safeUrl).nameWithoutExtension

        val file = File(cache.cacheDir, fileName)
        val imageDownloadResponse = downloadImage(safeUrl)
        if (imageDownloadResponse.isSuccessful) {
            imageDownloadResponse.body()?.let {
                saveImageFile(it, file.path)
            }
        } else {
            // todo: handle errors; for now just ignore
            return null
        }

        return file.path
    }

    private suspend fun search(latitude: Double, longitude: Double) =
        flickrService.search(lat = latitude, lng = longitude)

    private suspend fun downloadImage(url: String): Response<ResponseBody> {
        return flickrService.downloadImage(url)
    }

    private suspend fun saveImageFile(imageBody: ResponseBody, path: String) {
        withContext(Dispatchers.IO) {
            var input: InputStream? = null
            try {
                input = imageBody.byteStream()
                val fos = FileOutputStream(path)
                fos.use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                input?.close()
            }
        }
    }
}