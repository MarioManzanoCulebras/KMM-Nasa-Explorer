package com.mariomanzano.kmm_nasa_explorer.network

import kotlinx.datetime.LocalDate

/*
 * NASA moved the Astronomy Picture of the Day from https://api.nasa.gov/planetary/apod to the
 * WordPress REST API of science.nasa.gov in September 2026. The new endpoint needs no API key.
 *
 *   GET wp-json/wp/v2/apod-basic/{yymmdd}                       one picture
 *   GET wp-json/wp/v2/apod-basic?date_from={yymmdd}&date_to=... a page of pictures, newest first
 */
internal const val APOD_BASE_URL = "https://science.nasa.gov/"
internal const val APOD_PATH = "wp-json/wp/v2/apod-basic"

/** The server never returns more than 25 pictures per page, whatever `per_page` asks for. */
internal const val APOD_PAGE_SIZE = 25

/** Number of pages of the whole result, sent with every page of a list. */
internal const val APOD_TOTAL_PAGES_HEADER = "X-WP-TotalPages"

/** Only the fields the app reads, so the response does not carry the HTML of every page. */
internal const val APOD_FIELDS = "date,title,explanation,media_type,url,hdurl"

/** Formats a date the way the APOD endpoint expects it: 2026-11-10 becomes 261110. */
internal fun LocalDate.toApodDate(): String =
    twoDigits(year % 100) + twoDigits(monthNumber) + twoDigits(dayOfMonth)

private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')
