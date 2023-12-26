package com.mariomanzano.kmm_nasa_explorer.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem

@Composable
fun NasaItemDetailScreen(nasaItem: NasaItem?, onFavoriteClick: () -> Unit = {}) {
    nasaItem?.let {
        NasaItemDetailScaffold(
            nasaItem = it,
            onFavoriteClick = onFavoriteClick
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                item {
                    Header(nasaItem = it)
                }
            }
        }
    }
}

@Composable
private fun Header(nasaItem: NasaItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        if (nasaItem.mediaType == "image") {
            NasaImageWithLoader(
                urlImage = nasaItem.url,
                contentScale = ContentScale.Fit,
                contentDescription = nasaItem.title
            )
        } else {
            IconButton(
                onClick = { /* Todo: openYoutubeWithUrl(nasaItem.url, context)*/ },
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
                    contentDescription = "",
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = nasaItem.date,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp)
        )
        Text(
            text = nasaItem.title ?: "",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        SelectionContainer {
            Text(
                text = buildAnnotatedString {
                    append(
                        AnnotatedString(
                            text = nasaItem.description ?: "",
                            paragraphStyle = ParagraphStyle(
                                textIndent = TextIndent(
                                    firstLine = 25.sp,
                                    restLine = 10.sp
                                )
                            )
                        )
                    )
                },
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}
