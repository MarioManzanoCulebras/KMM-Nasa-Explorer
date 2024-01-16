package com.mariomanzano.kmm_nasa_explorer.shared.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:NasaDatabase.db")
        NasaDatabase.Schema.create(driver)
        return driver
    }
}