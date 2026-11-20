package com.mariomanzano.kmm_nasa_explorer.data

import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.APOD_PLACEHOLDER_IMAGE
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/*
 * Versions of the app that used https://api.nasa.gov/planetary/apod stored two kinds of rows that
 * no longer work. Keep these rules in sync with the getPODsNeedingRepair query in NasaDatabase.sq.
 */

/** Host of the images returned by the legacy API. It now redirects every image to an HTML page. */
internal const val LEGACY_IMAGE_PATH = "://apod.nasa.gov/apod/image/"

/** Image of the placeholder entry the legacy API returns for every date since the migration. */
internal const val LEGACY_PLACEHOLDER_URL =
    "https://science.nasa.gov/wp-content/themes/nasa-child/assets/images/nasa-logo@2x.png"

/** Host of the images of the new API. */
private const val NEW_IMAGE_PREFIX = "https://assets.science.nasa.gov/"

private const val IMAGE_MEDIA_TYPE = "image"

/** The row is a placeholder entry of the legacy API: its title and text are wrong too. */
internal fun PictureOfDayItem.isPlaceholderRow(): Boolean = url == LEGACY_PLACEHOLDER_URL

/** The row is a picture whose image is on the legacy host. Its title and text are still right. */
internal fun PictureOfDayItem.isLegacyImageRow(): Boolean =
    mediaType == IMAGE_MEDIA_TYPE && LEGACY_IMAGE_PATH in url

/**
 * The [stored] row fixed with what the new API returned for the same date ([fresh]), or null when
 * [fresh] cannot fix it, so that the row is tried again later.
 *
 * Legacy pictures keep their title and text and only take the new image. Placeholder rows take the
 * whole content. The favorite mark and the identity of the row are always kept.
 */
internal fun repairedFrom(stored: PictureOfDayItem, fresh: PictureOfDayItem): PictureOfDayItem? =
    when {
        stored.isPlaceholderRow() -> {
            val freshIsUsable = fresh.title.isNotBlank() &&
                    (fresh.mediaType != IMAGE_MEDIA_TYPE || fresh.hasUsableImage())
            if (freshIsUsable) {
                stored.copy(
                    title = fresh.title,
                    description = fresh.description,
                    url = fresh.url,
                    mediaType = fresh.mediaType
                )
            } else {
                null
            }
        }

        stored.isLegacyImageRow() ->
            if (fresh.mediaType == IMAGE_MEDIA_TYPE && fresh.hasUsableImage()) {
                stored.copy(url = fresh.url)
            } else {
                null
            }

        else -> null
    }

private fun PictureOfDayItem.hasUsableImage(): Boolean =
    url.startsWith(NEW_IMAGE_PREFIX) && APOD_PLACEHOLDER_IMAGE !in url

/**
 * Groups [dates] into the date ranges to request, each spanning at most [maxDays] days, so that
 * consecutive days share a request. The order of [dates] is kept: the first date opens the first
 * range, and a date that does not fit the current range opens the next one.
 */
internal fun repairWindows(dates: List<LocalDate>, maxDays: Int): List<ClosedRange<LocalDate>> {
    require(maxDays > 0) { "maxDays must be positive" }
    val windows = mutableListOf<ClosedRange<LocalDate>>()
    var start: LocalDate? = null
    var end: LocalDate? = null
    for (date in dates) {
        val currentStart = start
        val currentEnd = end
        if (currentStart == null || currentEnd == null) {
            start = date
            end = date
            continue
        }
        val newStart = minOf(currentStart, date)
        val newEnd = maxOf(currentEnd, date)
        if (newStart.daysUntil(newEnd) < maxDays) {
            start = newStart
            end = newEnd
        } else {
            windows += currentStart..currentEnd
            start = date
            end = date
        }
    }
    if (start != null && end != null) windows += start..end
    return windows
}
