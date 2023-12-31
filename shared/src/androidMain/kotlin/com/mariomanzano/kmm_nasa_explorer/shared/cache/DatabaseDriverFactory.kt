package com.mariomanzano.kmm_nasa_explorer.shared.cache

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(NasaDatabase.Schema, context, "NasaDatabase.db")
    }
}