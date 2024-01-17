package com.mariomanzano.kmm_nasa_explorer

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mariomanzano.kmm_nasa_explorer.shared.cache.NasaDatabase
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

class DesktopPlatform : Platform {
    override val name: String = "Desktop"
}

actual val platformModule: Module = module {
    single<SqlDriver> {
        val driver = JdbcSqliteDriver("jdbc:sqlite:database/NasaDatabase.db")
        if (!File("database/NasaDatabase.db").exists()) {
            NasaDatabase.Schema.create(driver)
            driver
        } else {
            driver
        }
    }
}


actual fun getPlatform(): Platform = DesktopPlatform()

@OptIn(ExperimentalSerializationApi::class)
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(CIO) {
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
