package com.mariomanzano.kmm_nada_explorer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mariomanzano.kmm_nada_explorer.ui.common.NasaItemDetailScreen
import com.mariomanzano.kmm_nada_explorer.ui.common.PODItemsListScreen
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureDetailViewModel
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DailyPictureScreen(
    listState: LazyListState,
    onClick: (PictureOfDayItem) -> Unit,
    onItemsMoreClicked: () -> Unit,
    viewModel: DailyPictureViewModel
) {
    val state by viewModel.state.collectAsState()

    PODItemsListScreen(
        loading = state.loading,
        items = state.dailyPictures,
        onClick = onClick,
        onRefreshComplete = { /* TODO: Complete when database is available */ },
        onSimpleRefresh = { /* TODO: Complete when database is available */ },
        state.error,
        listState = listState,
        onItemsMoreClicked = onItemsMoreClicked
    )
}

@Composable
fun DailyPictureDetailScreen(viewModel: DailyPictureDetailViewModel) {
    val state by viewModel.state.collectAsState()

    NasaItemDetailScreen(
        nasaItem = state.dailyPicture
    ) { /* TODO: Complete when database is available */ }
}