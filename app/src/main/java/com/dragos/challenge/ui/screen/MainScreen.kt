package com.dragos.challenge.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dragos.challenge.R
import com.dragos.challenge.ui.MainUiState
import com.dragos.challenge.ui.theme.DragosChallengeTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(
    state: State<MainUiState>,
    messageFlow: SharedFlow<String>,
    onStartStopButtonClick: () -> Unit
) {
    val isActive = state.value.isActive ?: false
    val images = state.value.images.reversed()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        messageFlow.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                contentPadding = PaddingValues(16.dp),
                elevation = 4.dp,
            ) {
                ButtonBar(isStarted = isActive) {
                    onStartStopButtonClick()
                }
            }
        }
    ) {
        Column {
            ImageGallery(images = images)
        }
    }
}

@Composable
fun ButtonBar(
    isStarted: Boolean,
    onButtonClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
        )
        Button(onClick = onButtonClick) {
            Text(
                stringResource(
                    id = if (isStarted) {
                        R.string.stop_label
                    } else {
                        R.string.start_label
                    }
                )
            )
        }
    }
}

@Composable
fun ImageGallery(
    images: List<String>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(images) { item ->
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.toUri())
                        .addHeader("User-Agent", "curl/7.79.1")
                        .crossfade(true)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .build(),
                ),
                contentScale = ContentScale.FillWidth,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.image_height))
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val state = remember {
        mutableStateOf(MainUiState(false, listOf()))
    }
    DragosChallengeTheme {
        MainScreen(state = state, MutableSharedFlow()) {
        }
    }
}