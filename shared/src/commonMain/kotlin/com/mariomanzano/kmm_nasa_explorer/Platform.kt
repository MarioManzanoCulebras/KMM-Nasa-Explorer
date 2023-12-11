package com.mariomanzano.kmm_nasa_explorer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform