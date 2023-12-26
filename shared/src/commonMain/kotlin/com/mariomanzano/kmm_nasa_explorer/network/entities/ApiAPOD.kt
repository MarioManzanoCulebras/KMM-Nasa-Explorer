package com.mariomanzano.kmm_nasa_explorer.network.entities

import kotlinx.serialization.Serializable

@Serializable
data class ApiAPOD(
    val date: String,
    val title: String?,
    val explanation: String?,
    val media_type: String?,
    val url: String?
)
