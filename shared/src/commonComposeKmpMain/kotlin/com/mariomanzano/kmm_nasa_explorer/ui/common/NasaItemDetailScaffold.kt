package com.mariomanzano.kmm_nasa_explorer.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NasaItemDetailScaffold(
    nasaItem: NasaItem,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            Row {
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = { onBack() }
                ) {
                    BuildLightIcon(
                        icon = Icons.Default.Info,
                        nasaIcon = NasaIcon.ArrowBack
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                FloatingActionButton(onClick = onFavoriteClick) {
                    if (nasaItem.favorite) BuildLightIcon(
                        icon = Icons.Default.Favorite,
                        nasaIcon = NasaIcon.FavoriteOn
                    )
                    else BuildLightIcon(
                        icon = Icons.Default.FavoriteBorder,
                        nasaIcon = NasaIcon.FavoriteOff
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        content = content
    )
}
