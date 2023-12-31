package com.mariomanzano.kmm_nasa_explorer.shared.cache

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}