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

@Composable
fun NasaItemDetailScaffold(
    nasaItem: NasaItem? = null,
    onBack: (() -> Unit)? = null,
    onFavoriteClick: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            Row {
                if (onBack != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(onClick = onBack) {
                        BuildLightIcon(
                            icon = Icons.Default.Info,
                            nasaIcon = NasaIcon.ArrowBack
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (onFavoriteClick != null) {
                    FloatingActionButton(onClick = onFavoriteClick) {
                        if (nasaItem != null) {
                            if (nasaItem.favorite) BuildLightIcon(
                                icon = Icons.Default.Favorite,
                                nasaIcon = NasaIcon.FavoriteOn
                            )
                            else BuildLightIcon(
                                icon = Icons.Default.FavoriteBorder,
                                nasaIcon = NasaIcon.FavoriteOff
                            )
                        } else {
                            BuildLightIcon(
                                icon = Icons.Default.Favorite,
                                nasaIcon = NasaIcon.FavoriteOn
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        content = content
    )
}
