package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.network.entities.ApiAPOD
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DailyPicturesService {
    suspend fun getPictureOfTheDay(date: String): ApiAPOD

    suspend fun getPicturesOfDateRange(startDate: String, endDate: String): List<ApiAPOD>

}

class DailyPicturesServiceImpl :
    DailyPicturesService, KoinComponent {

    private val ktor: HttpClient by inject()
    override suspend fun getPictureOfTheDay(date: String): ApiAPOD {
        return ktor.get("/planetary/apod") {
            parameter("date", date)
        }.body()
    }

    override suspend fun getPicturesOfDateRange(startDate: String, endDate: String): List<ApiAPOD> {
        return ktor.get("/planetary/apod") {
            parameter("start_date", startDate)
            parameter("end_date", endDate)
        }.body()
    }
}