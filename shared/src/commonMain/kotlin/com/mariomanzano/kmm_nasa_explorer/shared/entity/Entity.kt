package com.mariomanzano.kmm_nasa_explorer.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DbPOD(
    @SerialName("id")
    val id: Int,
    @SerialName("date")
    val date: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("url")
    val url: String,
    @SerialName("media_type")
    val mediaType: String,
    @SerialName("favorite")
    var favorite: Boolean,
    @SerialName("type")
    val type: String
)