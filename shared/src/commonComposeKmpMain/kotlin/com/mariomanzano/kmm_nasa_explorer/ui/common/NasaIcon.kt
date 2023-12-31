package com.mariomanzano.kmm_nasa_explorer.ui.common

sealed class NasaIcon(val resourceId: String) {
    object ArrowBack : NasaIcon("arrow_ios_back.xml")
    object Spacecraft : NasaIcon("ic_spacecraft.xml")

    object RocketVertical : NasaIcon("ic_rocket.xml")

    object Earth : NasaIcon("ic_earth.xml")

    object NasaLogo : NasaIcon("ic_nasa_logo.xml")

    object FavoriteMenu : NasaIcon("ic_favorite_off.xml")

    object FavoriteOn : NasaIcon("ic_favorite_on_light.xml")

    object FavoriteOff : NasaIcon("ic_favorite_off_light.xml")
}