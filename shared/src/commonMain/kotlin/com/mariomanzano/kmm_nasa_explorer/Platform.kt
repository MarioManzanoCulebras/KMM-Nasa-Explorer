package com.mariomanzano.kmm_nasa_explorer

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import org.koin.core.module.Module

interface Platform {
    val name: String
}

expect val platformModule: Module

expect fun getPlatform(): Platform

expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient