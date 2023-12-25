package com.mariomanzano.kmm_nada_explorer.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem

@Composable
fun NasaItemDetailScaffold(
    nasaItem: NasaItem,
    onFavoriteClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        content = content
    )
}
