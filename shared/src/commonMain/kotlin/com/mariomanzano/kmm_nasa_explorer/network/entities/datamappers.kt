package com.mariomanzano.kmm_nasa_explorer.network.entities

import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.datetime.LocalDateTime

fun ApiAPOD.asPictureOfTheDayItem() = PictureOfDayItem(
        id = (LocalDateTime.parse(date + "T00:00:00").date.dayOfYear.toString() +
                LocalDateTime.parse(date + "T00:00:00").date.year.toString()).toInt(),
        title = title ?: "",
        date = date,
        description = explanation ?: "",
        url = url ?: "",
        mediaType = media_type ?: ""
)
