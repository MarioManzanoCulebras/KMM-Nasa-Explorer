package com.mariomanzano.kmm_nasa_explorer

sealed interface Error {
    class Server(val code: Int) : Error
    object Connectivity : Error
    object NoData : Error
    class Unknown(val message: String) : Error
}