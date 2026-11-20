package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.network.entities.ApiAPOD
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
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
        return json.decodeFromString<ApiAPOD>(response).withRealImage()
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
            pictures += pagePictures.map { it.withRealImage() }

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

    /**
     * Some days come with a generic NASA picture as `hdurl` instead of their own image. Their web
     * page still links the real image, so it is taken from there. When the page cannot be read or
     * links no image, `hdurl` is removed rather than showing the generic picture.
     */
    private suspend fun ApiAPOD.withRealImage(): ApiAPOD {
        if (media_type != IMAGE_MEDIA_TYPE || hdurl?.contains(APOD_PLACEHOLDER_IMAGE) != true) {
            return this
        }
        val image = try {
            findImageInPage(date)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        return copy(hdurl = image)
    }

    private suspend fun findImageInPage(date: String): String? {
        val day = LocalDate.parse(date).toApodDate()
        val response = ktor.get("$APOD_BASE_URL$APOD_PATH/$day") {
            expectSuccess = true
            parameter("_fields", APOD_PAGE_FIELDS)
        }.bodyAsText()
        val html = json.decodeFromString<ApiAPODPage>(response).basic_html ?: return null
        return findLinkedImage(html)
    }

    private companion object {
        const val IMAGE_MEDIA_TYPE = "image"
    }
}

@Serializable
private data class ApiAPODPage(val basic_html: String? = null)

private val linkedImage = Regex(
    "(?:href|src)\\s*=\\s*\"(https://assets\\.science\\.nasa\\.gov/[^\"]+?\\.(?:jpe?g|png|gif))\"",
    RegexOption.IGNORE_CASE
)

/** The first image of the NASA asset host linked from [html], other than the generic picture. */
internal fun findLinkedImage(html: String): String? =
    linkedImage.findAll(html)
        .map { it.groupValues[1] }
        .firstOrNull { APOD_PLACEHOLDER_IMAGE !in it }
