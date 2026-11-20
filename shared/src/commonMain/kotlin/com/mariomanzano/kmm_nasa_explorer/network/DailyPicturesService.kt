package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.network.entities.ApiAPOD
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
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
        val day = LocalDate.parse(date).toApodDate()
        val response = ktor.get("$APOD_BASE_URL$APOD_PATH/$day") {
            expectSuccess = true
            parameter("_fields", APOD_FIELDS)
        }.bodyAsText()
        return json.decodeFromString<ApiAPOD>(response)
    }

    override suspend fun getPicturesOfDateRange(startDate: String, endDate: String): List<ApiAPOD> {
        val from = LocalDate.parse(startDate)
        val to = LocalDate.parse(endDate)
        // There is one picture per day, so the range cannot span more pages than this.
        val maxPages = maxOf(1, (from.daysUntil(to) + APOD_PAGE_SIZE) / APOD_PAGE_SIZE)

        val pictures = mutableListOf<ApiAPOD>()
        var page = 1
        do {
            val response = ktor.get("$APOD_BASE_URL$APOD_PATH") {
                expectSuccess = true
                parameter("date_from", from.toApodDate())
                parameter("date_to", to.toApodDate())
                parameter("per_page", APOD_PAGE_SIZE)
                parameter("page", page)
                parameter("_fields", APOD_FIELDS)
            }
            val pagePictures = json.decodeFromString<List<ApiAPOD>>(response.bodyAsText())
            pictures += pagePictures

            val totalPages = response.headers[APOD_TOTAL_PAGES_HEADER]?.toIntOrNull()
            val hasMorePages = when {
                pagePictures.isEmpty() -> false
                totalPages != null -> page < totalPages
                else -> pagePictures.size >= APOD_PAGE_SIZE
            }
            page++
        } while (hasMorePages && page <= maxPages)
        return pictures
    }
}
