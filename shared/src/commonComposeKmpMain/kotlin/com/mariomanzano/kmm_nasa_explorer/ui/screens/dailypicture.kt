package com.mariomanzano.kmm_nasa_explorer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.ui.common.NasaItemDetailScaffold
import com.mariomanzano.kmm_nasa_explorer.ui.common.NasaItemDetailScreen
import com.mariomanzano.kmm_nasa_explorer.ui.common.PODItemsListScreen
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureDetailViewModel
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
actual fun DailyPicture(
    onClick: (PictureOfDayItem) -> Unit,
    onItemsMoreClicked: () -> Unit,
    viewModel: DailyPictureViewModel
) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    NasaItemDetailScaffold(
        onFavoriteClick = { navigator.push(FavoritesPicturesScreen) }
    ) { padding ->
        PODItemsListScreen(
            loading = state.loading,
            items = state.dailyPictures,
            onClick = onClick,
            onRefreshComplete = { /* TODO: Complete when database is available */ },
            onSimpleRefresh = { /* TODO: Complete when database is available */ },
            state.error,
            listState = rememberLazyListState(),
            onItemsMoreClicked = onItemsMoreClicked
        )
    }
}

@Composable
actual fun DailyPictureDetailScreen(viewModel: DailyPictureDetailViewModel, onClose: () -> Unit) {
    val state by viewModel.state.collectAsState()

    NasaItemDetailScreen(
        nasaItem = state.dailyPicture,
        onBack = onClose,
        onFavoriteClick = { viewModel.onFavoriteClicked() }
    )
}