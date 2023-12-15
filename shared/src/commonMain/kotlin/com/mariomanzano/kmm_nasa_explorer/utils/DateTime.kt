package com.mariomanzano.kmm_nasa_explorer.utils

expect class DateTime() {
    fun getFormattedDate(iso8601Timestamp: String, format: String): String
}