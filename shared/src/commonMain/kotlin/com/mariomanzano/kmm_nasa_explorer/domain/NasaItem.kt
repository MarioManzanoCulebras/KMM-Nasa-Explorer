package com.mariomanzano.kmm_nasa_explorer.domain

interface NasaItem {
    var id: Int
    val date: String
    val title: String?
    val description: String?
    val url: String?
    val mediaType: String?
    var favorite: Boolean
    val type: String
}