package com.mariomanzano.kmm_nasa_explorer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.ui.common.NasaItemDetailScaffold
import com.mariomanzano.kmm_nasa_explorer.ui.common.NasaItemDetailScreen
import com.mariomanzano.kmm_nasa_explorer.ui.common.NasaItemsListScreen
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.FavoriteDetailViewModel
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.FavoriteViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun FavoritesScreen(
    onClick: (NasaItem) -> Unit,
    onItemsMoreClicked: () -> Unit,
    viewModel: FavoriteViewModel
) {
    val state by viewModel.state.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    viewModel.getFavorites()

    NasaItemDetailScaffold(
        onBack = { navigator.pop() }
    ) { padding ->
        NasaItemsListScreen(
            loading = state.loading,
            items = state.items,
            onClick = onClick,
            error = state.error,
            listState = rememberLazyGridState(),
            onItemsMoreClicked = onItemsMoreClicked
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun FavoritesDetailScreen(viewModel: FavoriteDetailViewModel) {
    val state by viewModel.state.collectAsState()

    NasaItemDetailScreen(
        nasaItem = state.nasaItem
    ) { viewModel.onFavoriteClicked() }
}