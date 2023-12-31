package com.mariomanzano.kmm_nasa_explorer.shared.cache

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(NasaDatabase.Schema, "NasaDatabase.db")
    }
}