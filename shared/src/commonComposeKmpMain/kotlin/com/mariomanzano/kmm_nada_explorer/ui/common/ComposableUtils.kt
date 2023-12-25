package com.mariomanzano.kmm_nada_explorer.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url

@Composable
fun NasaImageWithLoader(
    modifier: Modifier? = null, urlImage: String?,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    if (!urlImage.isNullOrBlank()) {
        asyncPainterResource(data = Url(urlImage))
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            /* Todo: resources multiplatform
            Image(
                painterResource(NasaIcon.NasaLogo.resourceId),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterHorizontally)
            )
            */
            Text(
                text = "Image Not Found",
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}