package com.mariomanzano.kmm_nasa_explorer.testdoubles

import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem

/** Rows as stored by versions of the app that used the legacy API, and their fixed versions. */
object LegacyRows {

    const val PLACEHOLDER_URL =
        "https://science.nasa.gov/wp-content/themes/nasa-child/assets/images/nasa-logo@2x.png"

    /** A picture whose image is on the legacy host, with the text the legacy API gave it. */
    fun legacyImage(date: String, favorite: Boolean = false): PictureOfDayItem = pictureOfDay(
        date = date,
        title = "Legacy title $date",
        description = "Legacy description $date",
        url = "https://apod.nasa.gov/apod/image/${yymm(date)}/Picture_${date}_960.jpg",
        mediaType = "image",
        favorite = favorite
    )

    /** A video stored from the legacy API: its link still works. */
    fun legacyVideo(date: String, favorite: Boolean = false): PictureOfDayItem = pictureOfDay(
        date = date,
        title = "Legacy video $date",
        description = "Legacy video description $date",
        url = "https://www.youtube.com/embed/sample?rel=0",
        mediaType = "video",
        favorite = favorite
    )

    /** What the legacy API now returns for every date. */
    fun placeholder(date: String, favorite: Boolean = false): PictureOfDayItem = pictureOfDay(
        date = date,
        title = "NASA Science",
        description = "Text of another day.",
        url = PLACEHOLDER_URL,
        mediaType = "image",
        favorite = favorite
    )

    fun newImage(date: String): String =
        "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/$date/Picture_$date.jpg" +
                "/jcr:content/renditions/cq5dam.web.1280.1280.jpeg"

    /** The item the current API gives for [date], as mapped by the app. */
    fun fresh(date: String, mediaType: String = "image"): PictureOfDayItem = pictureOfDay(
        date = date,
        title = "New title $date",
        description = "New description $date",
        url = newImage(date),
        mediaType = mediaType
    )

    private fun yymm(date: String) = date.substring(2, 4) + date.substring(5, 7)
}
