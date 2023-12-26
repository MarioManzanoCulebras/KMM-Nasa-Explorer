package com.mariomanzano.kmm_nasa_explorer.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalFoundationApi
@Composable
fun <T : NasaItem> NasaItemsListScreen(
    loading: Boolean = false,
    items: List<T>?,
    onClick: (T) -> Unit,
    onRefreshComplete: (() -> Unit)? = null,
    error: Error? = null,
    listState: LazyGridState,
    onItemsMoreClicked: () -> Unit,
) {
    if (error != null && !loading && (items == null || items.isEmpty())) {
        ErrorMessage(error = error, onRefreshComplete)
    } else {
        var bottomSheetItem by remember { mutableStateOf<T?>(null) }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val scope = rememberCoroutineScope()

        /* Todo: BackHandler on multiplatform project
        BackHandler(sheetState.isVisible) {
            scope.launch { sheetState.hide() }
        }*/

        ModalBottomSheetLayout(
            sheetContent = {
                NasaItemBottomPreview(
                    item = bottomSheetItem,
                    onGoToDetail = {
                        scope.launch {
                            sheetState.hide()
                            onClick(it)
                        }
                    }
                )
            },
            sheetState = sheetState
        ) {
            NasaItemsList(
                loading = loading,
                items = items,
                onItemClick = onClick,
                onItemMore = {
                    onItemsMoreClicked.invoke()
                    scope.launch {
                        bottomSheetItem = it
                        sheetState.show()
                    }
                },
                listState = listState
            )
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun PODItemsListScreen(
    loading: Boolean = false,
    items: List<PictureOfDayItem>?,
    onClick: (PictureOfDayItem) -> Unit,
    onRefreshComplete: () -> Unit,
    onSimpleRefresh: () -> Unit,
    error: Error?,
    listState: LazyListState,
    onItemsMoreClicked: () -> Unit
) {
    if (error != null && !loading && items?.isEmpty() == true) {
        ErrorMessage(error = error, onRefreshComplete)
    } else {
        var bottomSheetItem by remember { mutableStateOf<PictureOfDayItem?>(null) }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val scope = rememberCoroutineScope()

        /* Todo: BackHandler on multiplatform project
        BackHandler(sheetState.isVisible) {
            scope.launch { sheetState.hide() }
        }
        */

        ModalBottomSheetLayout(
            sheetContent = {
                NasaItemBottomPreview(
                    item = bottomSheetItem,
                    onGoToDetail = {
                        scope.launch {
                            sheetState.hide()
                            onClick(it)
                        }
                    }
                )
            },
            sheetState = sheetState
        ) {
            PODItemsList(
                loading = loading,
                items = items,
                onItemClick = onClick,
                onItemMore = {
                    onItemsMoreClicked.invoke()
                    scope.launch {
                        bottomSheetItem = it
                        sheetState.show()
                    }
                },
                listState = listState
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun <T : NasaItem> NasaItemsList(
    loading: Boolean,
    items: List<T>?,
    onItemClick: (T) -> Unit,
    onItemMore: (T) -> Unit,
    listState: LazyGridState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        if (loading) {
            CircularProgressIndicator()
        }

        items?.let { list ->
            if (list.isNotEmpty()) {
                LazyVerticalGrid(
                    state = listState,
                    columns = GridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {

                    items(list) {
                        NasaListItem(
                            nasaItem = it,
                            onItemMore = onItemMore,
                            showFooterInside = false,
                            modifier = Modifier.clickable { onItemClick(it) }
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun PODItemsList(
    loading: Boolean,
    items: List<PictureOfDayItem>?,
    onItemClick: (PictureOfDayItem) -> Unit,
    onItemMore: (PictureOfDayItem) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        if (loading) {
            CircularProgressIndicator()
        }

        items?.let { list ->
            if (list.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(list) {
                        NasaListItem(
                            nasaItem = it,
                            onItemMore = onItemMore,
                            showFooterInside = true,
                            modifier = Modifier.clickable { onItemClick(it) }
                        )
                    }
                }
            }
        }
    }
}