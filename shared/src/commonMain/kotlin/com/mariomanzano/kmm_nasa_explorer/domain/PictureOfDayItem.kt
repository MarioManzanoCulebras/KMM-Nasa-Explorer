package com.mariomanzano.kmm_nasa_explorer.domain

data class PictureOfDayItem(
    override var id: Int,
    override val date: String,
    override val title: String,
    override val description: String,
    override val url: String,
    override val mediaType: String,
    override var favorite: Boolean = false,
    override val type: String = "dailyPicture"
) : NasaItem
