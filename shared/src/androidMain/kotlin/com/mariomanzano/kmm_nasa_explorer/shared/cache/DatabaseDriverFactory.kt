package com.mariomanzano.kmm_nasa_explorer.shared.cache

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(NasaDatabase.Schema, context, "NasaDatabase.db")
    }
}