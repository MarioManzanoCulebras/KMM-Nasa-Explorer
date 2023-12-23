package com.mariomanzano.kmm_nasa_explorer.network.entities

import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem

fun ApiAPOD.asPictureOfTheDayItem() = PictureOfDayItem(
        id = 0,
        title = title ?: "",
        date = date,
        description = explanation ?: "",
        url = url ?: "",
        copyRight = copyRight ?: "",
        mediaType = media_type ?: ""
)
