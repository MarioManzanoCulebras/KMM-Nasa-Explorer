package com.mariomanzano.kmm_nasa_explorer

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

@Composable
internal expect fun getArrowBack(): Painter