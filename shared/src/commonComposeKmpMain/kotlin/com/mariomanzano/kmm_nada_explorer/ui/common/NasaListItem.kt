package com.mariomanzano.kmm_nada_explorer.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem

@Composable
fun <T : NasaItem> NasaListItem(
    nasaItem: T,
    onItemMore: (T) -> Unit,
    showFooterInside: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp)
    ) {
        Card {
            Box {
                if (nasaItem.mediaType == "image") {
                    NasaImageWithLoader(urlImage = nasaItem.url)
                } else {
                    IconButton(
                        onClick = { /* Todo: openYoutubeWithUrl(nasaItem.url, context) */ },
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colors.primaryVariant,
                                        MaterialTheme.colors.secondary
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = ""
                        )
                    }
                }
                if (showFooterInside) {
                    Surface(
                        color = Color(0x803F3F3F), // Todo color: grey_with_alpha
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        FooterRow(nasaItem = nasaItem, onItemMore = onItemMore)
                    }
                }
            }
        }
        if (!showFooterInside) {
            FooterRow(nasaItem = nasaItem, onItemMore = onItemMore)
        }
    }
}

@Composable
fun <T : NasaItem> FooterRow(nasaItem: T, onItemMore: (T) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onItemMore(nasaItem)
        }
    ) {
        Text(
            text = nasaItem.title ?: "",
            style = MaterialTheme.typography.subtitle1,
            maxLines = 2,
            modifier = Modifier
                .padding(8.dp, 16.dp)
                .weight(1f)
        )
        IconButton(onClick = { onItemMore(nasaItem) }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options" // Todo: R.string.more_options
            )
        }
    }
}
