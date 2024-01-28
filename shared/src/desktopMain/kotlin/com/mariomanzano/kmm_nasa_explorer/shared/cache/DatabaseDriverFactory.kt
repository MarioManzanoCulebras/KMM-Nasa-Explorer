package com.mariomanzano.kmm_nasa_explorer.shared.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite::resource:database/NasaDatabase.db")
        return if (!File("database/NasaDatabase.db").exists()) {
            NasaDatabase.Schema.create(driver)
            driver
        } else {
            driver
        }
    }
}