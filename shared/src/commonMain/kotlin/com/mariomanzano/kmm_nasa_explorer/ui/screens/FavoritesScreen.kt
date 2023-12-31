package com.mariomanzano.kmm_nasa_explorer.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.FavoriteViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object FavoritesPicturesScreen : Screen, KoinComponent {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: FavoriteViewModel by inject()
        FavoritesScreen(
            onClick = { pictureOfTheDay ->
                navigator.push(DailyPictureDetailScreen(pictureOfTheDay.id))
            },
            onItemsMoreClicked = { /* Todo */ },
            viewModel = viewModel
        )
    }

}

@Composable
expect fun FavoritesScreen(
    onClick: (NasaItem) -> Unit,
    onItemsMoreClicked: () -> Unit,
    viewModel: FavoriteViewModel
)