package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.network.entities.ApiAPOD
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DailyPicturesService {
    suspend fun getPictureOfTheDay(date: String): ApiAPOD

    suspend fun getPicturesOfDateRange(startDate: String, endDate: String): List<ApiAPOD>

}

class DailyPicturesServiceImpl :
    DailyPicturesService, KoinComponent {

    private val ktor: HttpClient by inject()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override suspend fun getPictureOfTheDay(date: String): ApiAPOD {
        val response = ktor.get("/planetary/apod") {
            url {
                parameters.append("api_key", "zHElzURyfsLM8SE1NLZTsoqjmjbg41vwFkVEpebG")
            }
            parameter("date", date)
        }.bodyAsText()
        return json.decodeFromString<ApiAPOD>(response)
    }

    override suspend fun getPicturesOfDateRange(startDate: String, endDate: String): List<ApiAPOD> {
        val response = ktor.get("/planetary/apod") {
            url {
                parameters.append("api_key", "zHElzURyfsLM8SE1NLZTsoqjmjbg41vwFkVEpebG")
            }
            parameter("start_date", startDate)
            parameter("end_date", endDate)
        }.bodyAsText()
        return json.decodeFromString<List<ApiAPOD>>(response)
    }
}