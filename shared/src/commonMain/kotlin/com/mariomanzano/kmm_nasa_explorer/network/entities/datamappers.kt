package com.mariomanzano.kmm_nasa_explorer.network.entities

import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.datetime.LocalDateTime

private const val IMAGE_MEDIA_TYPE = "image"

/** Path of the 48 pixel preview the API sometimes returns in place of the picture. */
private const val THUMBNAIL_RENDITION = "/jcr:content/renditions/cq5dam.thumbnail."

fun ApiAPOD.asPictureOfTheDayItem() = PictureOfDayItem(
        id = (LocalDateTime.parse(date + "T00:00:00").date.dayOfYear.toString() +
                LocalDateTime.parse(date + "T00:00:00").date.year.toString()).toInt(),
        title = title?.htmlToPlainText() ?: "",
        date = date,
        description = explanation?.apodExplanationToPlainText() ?: "",
        url = mediaUrl(),
        mediaType = media_type ?: ""
)

/**
 * The address the app shows: the image itself for pictures, the web page for anything else.
 *
 * `url` used to be the image, but it now is the web page of the picture and the image comes in
 * `hdurl`.
 */
private fun ApiAPOD.mediaUrl(): String {
    val image = hdurl?.takeIf { it.isNotBlank() }
    return if (media_type == IMAGE_MEDIA_TYPE && image != null) {
        image.toFullSizeImageUrl()
    } else {
        url ?: ""
    }
}

/** Replaces the 48 pixel preview rendition of an asset by the asset itself. */
internal fun String.toFullSizeImageUrl(): String {
    val rendition = indexOf(THUMBNAIL_RENDITION)
    return if (rendition >= 0) substring(0, rendition) else this
}
