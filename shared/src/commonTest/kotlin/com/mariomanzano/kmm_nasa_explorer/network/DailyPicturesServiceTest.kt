package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.testdoubles.ApodApiFixtures
import com.mariomanzano.kmm_nasa_explorer.testdoubles.respondJson
import com.mariomanzano.kmm_nasa_explorer.testdoubles.serviceBackedBy
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The requests the app sends to, and the answers it reads from, the APOD WordPress API. */
class DailyPicturesServiceTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    // region one picture

    @Test
    fun singleDayIsRequestedFromTheWordPressApiUsingTheSixDigitDate() = runTest {
        val engine = MockEngine { respondJson(ApodApiFixtures.SINGLE_DAY) }

        serviceBackedBy(engine).getPictureOfTheDay("2026-11-10")

        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals(URLProtocol.HTTPS, request.url.protocol)
        assertEquals("science.nasa.gov", request.url.host)
        assertEquals("/wp-json/wp/v2/apod-basic/261110", request.url.encodedPath)
    }

    @Test
    fun singleDayAsksOnlyForTheFieldsTheAppReads() = runTest {
        val engine = MockEngine { respondJson(ApodApiFixtures.SINGLE_DAY) }

        serviceBackedBy(engine).getPictureOfTheDay("2026-11-10")

        val parameters = engine.requestHistory.single().url.parameters
        assertEquals("date,title,explanation,media_type,url,hdurl", parameters["_fields"])
        assertEquals(setOf("_fields"), parameters.names())
    }

    @Test
    fun singleDayDoesNotSendTheLegacyKeyOrDateParameters() = runTest {
        val engine = MockEngine { respondJson(ApodApiFixtures.SINGLE_DAY) }

        serviceBackedBy(engine).getPictureOfTheDay("2026-11-10")

        val parameters = engine.requestHistory.single().url.parameters
        assertNull(parameters["api_key"])
        assertNull(parameters["date"])
    }

    @Test
    fun singleDayAnswerIsParsedIgnoringTheFieldsTheAppDoesNotUse() = runTest {
        val engine = MockEngine { respondJson(ApodApiFixtures.SINGLE_DAY) }

        val picture = serviceBackedBy(engine).getPictureOfTheDay("2026-11-10")

        assertEquals("2026-11-10", picture.date)
        assertEquals("Sample Nebula &amp; Friends", picture.title)
        assertEquals("image", picture.media_type)
        assertEquals(ApodApiFixtures.PAGE_URL, picture.url)
        assertEquals(ApodApiFixtures.IMAGE_URL, picture.hdurl)
        assertTrue(picture.explanation.orEmpty().startsWith("<strong>Explanation:</strong>"))
    }

    @Test
    fun singleDayFailsWithAClientErrorWhenThereIsNoPictureForThatDay() = runTest {
        val engine = MockEngine {
            respondJson(ApodApiFixtures.NOT_FOUND, status = HttpStatusCode.NotFound)
        }

        val failure = assertFailsWith<ClientRequestException> {
            serviceBackedBy(engine).getPictureOfTheDay("2027-01-01")
        }

        assertEquals(HttpStatusCode.NotFound, failure.response.status)
    }

    @Test
    fun singleDayFailsWithAServerErrorWhenTheServerIsUnavailable() = runTest {
        val engine = MockEngine {
            respondJson("upstream connect error", status = HttpStatusCode.ServiceUnavailable)
        }

        assertFailsWith<ServerResponseException> {
            serviceBackedBy(engine).getPictureOfTheDay("2026-11-10")
        }
    }

    @Test
    fun singleDayRejectsADateThatIsNotIsoFormattedWithoutCallingTheServer() = runTest {
        val engine = MockEngine { respondJson(ApodApiFixtures.SINGLE_DAY) }

        assertFailsWith<IllegalArgumentException> {
            serviceBackedBy(engine).getPictureOfTheDay("10/11/2026")
        }

        assertTrue(engine.requestHistory.isEmpty())
    }

    // endregion

    // region range of pictures

    @Test
    fun rangeIsRequestedFromTheWordPressApiWithSixDigitDatesAndPaging() = runTest {
        val engine = MockEngine { respondJson("[]", totalPages = 0) }

        serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")

        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("science.nasa.gov", request.url.host)
        assertEquals("/wp-json/wp/v2/apod-basic", request.url.encodedPath)
        val parameters = request.url.parameters
        assertEquals("260918", parameters["date_from"])
        assertEquals("261116", parameters["date_to"])
        assertEquals("25", parameters["per_page"])
        assertEquals("1", parameters["page"])
        assertEquals("date,title,explanation,media_type,url,hdurl", parameters["_fields"])
    }

    @Test
    fun rangeDoesNotSendTheLegacyKeyOrDateParameters() = runTest {
        val engine = MockEngine { respondJson("[]", totalPages = 0) }

        serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")

        val parameters = engine.requestHistory.single().url.parameters
        assertNull(parameters["api_key"])
        assertNull(parameters["start_date"])
        assertNull(parameters["end_date"])
    }

    @Test
    fun rangeAnswerIsParsed() = runTest {
        val dates = listOf("2026-11-12", "2026-11-11", "2026-11-10")
        val engine = MockEngine { respondJson(ApodApiFixtures.page(dates), totalPages = 1) }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2026-11-10", "2026-11-12")

        assertEquals(dates, pictures.map { it.date })
        assertEquals(ApodApiFixtures.imageUrl("2026-11-12"), pictures.first().hdurl)
        assertEquals(ApodApiFixtures.pageUrl("2026-11-12"), pictures.first().url)
    }

    @Test
    fun rangeDownloadsEveryPageAnnouncedByTheServer() = runTest {
        val dates = ApodApiFixtures.datesBackFrom(LocalDate(2026, 11, 16), count = 60)
        val pages = dates.chunked(25)
        val engine = MockEngine { request ->
            val page = request.url.parameters["page"]!!.toInt()
            respondJson(ApodApiFixtures.page(pages[page - 1]), totalPages = pages.size)
        }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")

        assertEquals(listOf("1", "2", "3"), engine.requestHistory.map { it.url.parameters["page"] })
        assertEquals(dates, pictures.map { it.date })
    }

    @Test
    fun rangeStopsAfterTheOnlyPage() = runTest {
        val dates = ApodApiFixtures.datesBackFrom(LocalDate(2026, 11, 16), count = 25)
        val engine = MockEngine { respondJson(ApodApiFixtures.page(dates), totalPages = 1) }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")

        assertEquals(1, engine.requestHistory.size)
        assertEquals(25, pictures.size)
    }

    @Test
    fun rangeKeepsAskingWhilePagesAreFullWhenTheServerDoesNotAnnounceThePages() = runTest {
        val dates = ApodApiFixtures.datesBackFrom(LocalDate(2026, 11, 16), count = 35)
        val pages = dates.chunked(25)
        val engine = MockEngine { request ->
            val page = request.url.parameters["page"]!!.toInt()
            respondJson(ApodApiFixtures.page(pages[page - 1]), totalPages = null)
        }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")

        assertEquals(2, engine.requestHistory.size)
        assertEquals(dates, pictures.map { it.date })
    }

    @Test
    fun rangeNeverAsksForMorePagesThanItsDaysCanFill() = runTest {
        val fullPage = ApodApiFixtures.datesBackFrom(LocalDate(2026, 11, 16), count = 25)
        val engine = MockEngine { respondJson(ApodApiFixtures.page(fullPage), totalPages = 99) }

        // 60 days fit in 3 pages of 25.
        serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")

        assertEquals(3, engine.requestHistory.size)
    }

    @Test
    fun rangeOfASingleDayNeedsOneRequest() = runTest {
        val engine = MockEngine {
            respondJson(ApodApiFixtures.page(listOf("2026-11-16")), totalPages = 99)
        }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2026-11-16", "2026-11-16")

        assertEquals(1, engine.requestHistory.size)
        assertEquals(listOf("2026-11-16"), pictures.map { it.date })
    }

    @Test
    fun rangeWithoutPicturesReturnsAnEmptyList() = runTest {
        val engine = MockEngine { respondJson("[]", totalPages = 0) }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2027-01-01", "2027-01-05")

        assertTrue(pictures.isEmpty())
        assertEquals(1, engine.requestHistory.size)
    }

    @Test
    fun rangeFailsWithAClientErrorWhenTheServerRejectsTheRequest() = runTest {
        val engine = MockEngine {
            respondJson(ApodApiFixtures.INVALID_DATES, status = HttpStatusCode.BadRequest)
        }

        assertFailsWith<ClientRequestException> {
            serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")
        }
    }

    // region generic pictures

    @Test
    fun singleDayWithTheGenericPictureTakesTheImageLinkedFromItsPage() = runTest {
        val engine = MockEngine { request ->
            if (request.url.parameters["_fields"] == "basic_html") {
                respondJson(ApodApiFixtures.pageLinking(ApodApiFixtures.linkedImage("1995-06-16")))
            } else {
                respondJson(ApodApiFixtures.pictureWithGenericImage("1995-06-16"))
            }
        }

        val picture = serviceBackedBy(engine).getPictureOfTheDay("1995-06-16")

        assertEquals(ApodApiFixtures.linkedImage("1995-06-16"), picture.hdurl)
        val pageRequest = engine.requestHistory.last()
        assertEquals("/wp-json/wp/v2/apod-basic/950616", pageRequest.url.encodedPath)
        assertEquals(2, engine.requestHistory.size)
    }

    @Test
    fun rangeAsksForThePageOfEachDayWithTheGenericPictureOnly() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.parameters["_fields"] == "basic_html" -> {
                    val day = request.url.encodedPath.substringAfterLast('/')
                    respondJson(ApodApiFixtures.pageLinking(ApodApiFixtures.linkedImage(day)))
                }

                else -> respondJson(
                    "[${ApodApiFixtures.picture("2019-10-05")}," +
                            "${ApodApiFixtures.pictureWithGenericImage("2019-10-04")}]",
                    totalPages = 1
                )
            }
        }

        val pictures = serviceBackedBy(engine).getPicturesOfDateRange("2019-10-04", "2019-10-05")

        assertEquals(ApodApiFixtures.imageUrl("2019-10-05"), pictures[0].hdurl)
        assertEquals(ApodApiFixtures.linkedImage("191004"), pictures[1].hdurl)
        assertEquals(
            listOf("/wp-json/wp/v2/apod-basic", "/wp-json/wp/v2/apod-basic/191004"),
            engine.requestHistory.map { it.url.encodedPath }
        )
    }

    @Test
    fun genericPictureIsDroppedWhenThePageLinksNoImage() = runTest {
        val engine = MockEngine { request ->
            if (request.url.parameters["_fields"] == "basic_html") {
                respondJson(ApodApiFixtures.pageLinking(null))
            } else {
                respondJson(ApodApiFixtures.pictureWithGenericImage("1995-06-16"))
            }
        }

        val picture = serviceBackedBy(engine).getPictureOfTheDay("1995-06-16")

        assertNull(picture.hdurl)
    }

    @Test
    fun genericPictureIsDroppedWhenThePageCannotBeRead() = runTest {
        val engine = MockEngine { request ->
            if (request.url.parameters["_fields"] == "basic_html") {
                respondJson("unavailable", status = HttpStatusCode.ServiceUnavailable)
            } else {
                respondJson(ApodApiFixtures.pictureWithGenericImage("1995-06-16"))
            }
        }

        val picture = serviceBackedBy(engine).getPictureOfTheDay("1995-06-16")

        assertEquals("1995-06-16", picture.date)
        assertNull(picture.hdurl)
    }

    @Test
    fun videoWithTheGenericPictureDoesNotAskForItsPage() = runTest {
        val engine = MockEngine {
            respondJson(ApodApiFixtures.pictureWithGenericImage("2023-12-03", mediaType = "video"))
        }

        val picture = serviceBackedBy(engine).getPictureOfTheDay("2023-12-03")

        assertEquals(1, engine.requestHistory.size)
        assertEquals(ApodApiFixtures.GENERIC_PICTURE, picture.hdurl)
    }

    @Test
    fun linkedImageIsTheFirstImageOfTheNasaAssetHost() {
        val html = """<img src="https://example.test/a.jpg">""" +
                """<a href="https://assets.science.nasa.gov/dynamicimage/x/images/misc/news-thumbnail.png">""" +
                """<a href="https://assets.science.nasa.gov/content/dam/apod/1995/june/e_lens.gif">""" +
                """<img SRC="https://assets.science.nasa.gov/content/dam/apod/second.jpg">"""

        assertEquals(
            "https://assets.science.nasa.gov/content/dam/apod/1995/june/e_lens.gif",
            findLinkedImage(html)
        )
        assertNull(findLinkedImage("""<a href="https://example.test/page.html">page</a>"""))
    }

    // endregion

    @Test
    fun rangeFailsWhenALaterPageFails() = runTest {
        val fullPage = ApodApiFixtures.datesBackFrom(LocalDate(2026, 11, 16), count = 25)
        val engine = MockEngine { request ->
            if (request.url.parameters["page"] == "1") {
                respondJson(ApodApiFixtures.page(fullPage), totalPages = 3)
            } else {
                respondJson("unavailable", status = HttpStatusCode.ServiceUnavailable)
            }
        }

        assertFailsWith<ServerResponseException> {
            serviceBackedBy(engine).getPicturesOfDateRange("2026-09-18", "2026-11-16")
        }
    }

    // endregion
}
