package com.mariomanzano.kmm_nasa_explorer.shared.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver


actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(NasaDatabase.Schema, "NasaDatabase.db")
    }
}