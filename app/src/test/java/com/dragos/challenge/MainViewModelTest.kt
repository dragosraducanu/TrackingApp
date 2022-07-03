package com.dragos.challenge

import com.dragos.challenge.data.repository.ImageRepository
import com.dragos.challenge.data.source.db.Location
import com.dragos.challenge.data.source.db.TrackingData
import com.dragos.challenge.rule.MainCoroutineRule
import com.dragos.challenge.ui.MainViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    private val imageRepository = mockk<ImageRepository>(relaxed = true)
    private lateinit var viewModel: MainViewModel

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    @Before
    fun setup() {
        viewModel = MainViewModel(imageRepository)
    }

    @Test
    fun testFetchingTheTrackingData() {
        coEvery {
            imageRepository.fetchTrackingData()
        } returns flowOf(TRACKING_DATA)

        viewModel.fetchTrackingData()
        assertEquals(TRACKING_DATA.locationList.map { it.imageUri }, viewModel.uiState.value.images)
    }

    @Test
    fun testActiveStateStaysNullOnFirstToggle() = runBlocking {
        viewModel.toggleTrackingState()

        assertEquals(null, viewModel.uiState.value.isActive)
    }

    @Test
    fun testToggleActiveState() = runBlocking {
        viewModel.setTrackingActive(true)

        viewModel.toggleTrackingState()

        assertEquals(false, viewModel.uiState.value.isActive)
    }

    @Test
    fun testTogglingToActiveClearsData() = runBlocking {
        viewModel.setTrackingActive(false)
        viewModel.toggleTrackingState()

        assertEquals(true, viewModel.uiState.value.isActive)
        coVerify {
            imageRepository.removeAllImages()
        }
    }

    @Test
    fun testActiveStateIsSet() = runBlocking {
        viewModel.setTrackingActive(true)

        assertEquals(true, viewModel.uiState.value.isActive)
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