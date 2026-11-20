package com.mariomanzano.kmm_nasa_explorer.network.entities

import kotlinx.serialization.Serializable

/**
 * A picture as returned by `wp-json/wp/v2/apod-basic`.
 *
 * [title] and [explanation] contain HTML. [url] is the web page of the picture and [hdurl] the
 * image itself (for videos, a preview image).
 */
@Serializable
data class ApiAPOD(
    val date: String,
    val title: String?,
    val explanation: String?,
    val media_type: String?,
    val url: String?,
    val hdurl: String? = null
)
