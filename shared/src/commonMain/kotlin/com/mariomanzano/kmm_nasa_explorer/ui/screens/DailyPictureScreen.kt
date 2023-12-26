package com.mariomanzano.kmm_nasa_explorer.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DailyPictureScreen : Screen, KoinComponent {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: DailyPictureViewModel by inject()
        DailyPicture(
            onClick = { pictureOfTheDay ->
                navigator.push(DailyPictureDetailScreen(pictureOfTheDay.id))
            },
            onItemsMoreClicked = { /* Todo */ },
            viewModel = viewModel
        )
    }

}

@Composable
expect fun DailyPicture(
    onClick: (PictureOfDayItem) -> Unit,
    onItemsMoreClicked: () -> Unit,
    viewModel: DailyPictureViewModel
)