package com.mariomanzano.kmm_nasa_explorer

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.defaultRequest
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Darwin) {
    config(this)
    expectSuccess = true
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
        this@HttpClient.defaultRequest {
            url("https://api.nasa.gov/")
        }
    }
}