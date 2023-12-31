package com.mariomanzano.kmm_nasa_explorer

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mariomanzano.kmm_nasa_explorer.shared.cache.NasaDatabase
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual val platformModule: Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = NasaDatabase.Schema,
            context = get(),
            name = "NasaDatabase.db"
        )
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()

@OptIn(ExperimentalSerializationApi::class)
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp) {
    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
    }
    defaultRequest {
        header("Content-Type", "application/json")
        url("https://api.nasa.gov/")
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
}
