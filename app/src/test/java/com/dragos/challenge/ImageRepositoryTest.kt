package com.dragos.challenge

import com.dragos.challenge.data.repository.ImageRepository
import com.dragos.challenge.data.source.api.FlickrService
import com.dragos.challenge.data.source.api.Image
import com.dragos.challenge.data.source.api.ImageSearchResult
import com.dragos.challenge.data.source.api.SearchResult
import com.dragos.challenge.data.source.db.CacheLocation
import com.dragos.challenge.data.source.db.Location
import com.dragos.challenge.data.source.db.TrackingData
import com.dragos.challenge.data.source.db.TrackingDataStore
import io.mockk.*
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class ImageRepositoryTest {

    private val dataStore = mockk<TrackingDataStore>()
    private val flickrService = mockk<FlickrService>()
    private val cache = mockk<CacheLocation>()
    private val file = mockk<File>()

    private lateinit var imageRepository: ImageRepository

    @Before
    fun setup() {

        coEvery {
            dataStore.saveLocation(any())
        } returnsArgument 0

        coEvery {
            dataStore.updateLocations(any())
        } just Runs

        coEvery {
            dataStore.removeAll()
        } just Runs

        coEvery {
            dataStore.fetchTrackingData()
        } returns flowOf(TRACKING_DATA)

        coEvery {
            flickrService.downloadImage(any())
        } returns Response.error(400, "".toResponseBody())

        coEvery {
            flickrService.search(lat = any(), lng = any())
        } returns ImageSearchResult(
            result = SearchResult(
                images = listOf(Image(
                    id = "id",
                    owner = "owner",
                    secret = "secret",
                    server = "server",
                    url = "url"
                ))
            )
        )

        every {
            cache.cacheDir
        } returns file


        imageRepository = ImageRepository(
            flickrService = flickrService,
            dataStore = dataStore,
            cache = cache
        )
    }

    @Test
    fun testFetchImageReturnsLocation() = runBlocking {
        val l = imageRepository.fetchImageForLatLng(1.0, 2.0)!!

        assertEquals(1.0, l.lat)
        assertEquals(2.0, l.lng)
        assertEquals("url", l.imageUrl)
    }

    @Test
    fun testSaveLocationSavesDatastore() = runBlocking {
        imageRepository.saveLocation(TRACKING_DATA.locationList.first())

        coVerify {
            dataStore.saveLocation(TRACKING_DATA.locationList.first())
        }
    }

    @Test
    fun testUpdateLocationUpdatesDataStore() = runBlocking {
        imageRepository.updateLocations(listOf(TRACKING_DATA.locationList.first()))

        coVerify {
            dataStore.updateLocations(listOf(TRACKING_DATA.locationList.first()))
        }
    }

    @Test
    fun testFetchesImagesFromDataStore() = runBlocking {
        val trackingData = mutableListOf<TrackingData>()
        val fl = imageRepository.fetchTrackingData().toList(trackingData)

        coVerify { dataStore.fetchTrackingData() }

        val actual = fl.first()
        assertEquals(TRACKING_DATA, actual)
    }

    @Test
    fun testRemoveAllRemovesFromDataStore() = runBlocking {
        imageRepository.removeAllImages()
        coVerify { dataStore.removeAll() }
    }

    companion object {
       private val TRACKING_DATA: TrackingData =
            TrackingData.newBuilder()
                .addLocation(
                    Location.newBuilder()
                        .setLat(1.0)
                        .setLng(2.0)
                        .setImageUri("img_uri")
                        .setImageUrl("img_url")
                        .setTimestamp(123L)
                        .build()
                )
                .build()
    }
}