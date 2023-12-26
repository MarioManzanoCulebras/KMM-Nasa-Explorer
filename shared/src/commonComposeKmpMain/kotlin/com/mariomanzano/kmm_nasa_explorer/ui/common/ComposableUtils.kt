package com.mariomanzano.kmm_nasa_explorer.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url

@Composable
fun NasaImageWithLoader(
    modifier: Modifier? = null, urlImage: String?,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    if (!urlImage.isNullOrBlank()) {
        KamelImage(
            resource = asyncPainterResource(data = Url(urlImage)),
            contentScale = contentScale,
            modifier = modifier ?: Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            onLoading = { progress ->
                CircularProgressIndicator(
                    progress = progress
                )
            },
            onFailure = {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Icon(
                        imageVector = (Icons.Default.Info),
                        contentDescription = "Image Not Found",
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Image Not Found",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

            },
            contentDescription = contentDescription
        )
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