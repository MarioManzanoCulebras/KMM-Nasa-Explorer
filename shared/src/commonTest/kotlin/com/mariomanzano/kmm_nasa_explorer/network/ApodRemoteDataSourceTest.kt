package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.testdoubles.ApodApiFixtures
import com.mariomanzano.kmm_nasa_explorer.testdoubles.respondJson
import com.mariomanzano.kmm_nasa_explorer.testdoubles.serviceBackedBy
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * From the HTTP answer of the APOD WordPress API to the items the app shows: the real service,
 * JSON parsing and mapping behind [PODServerDataSource], with only the network replaced.
 */
class ApodRemoteDataSourceTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun aPictureOfTheApiBecomesAnItemTheAppCanShow() = runTest {
        val engine = MockEngine { respondJson(ApodApiFixtures.SINGLE_DAY) }

        val item = dataSource(engine).findPODDay("2026-11-10").getOrNull()

        assertNotNull(item)
        assertEquals(3142026, item.id)
        assertEquals("2026-11-10", item.date)
        assertEquals("Sample Nebula & Friends", item.title)
        assertEquals(ApodApiFixtures.EXPLANATION_TEXT, item.description)
        assertEquals(ApodApiFixtures.IMAGE_URL, item.url)
        assertEquals("image", item.mediaType)
        assertFalse(item.favorite)
    }

    @Test
    fun theSixtyDaysOfTheListAreDownloadedPageByPageAndShownNewestFirst() = runTest {
        val dates = ApodApiFixtures.datesBackFrom(LocalDate(2026, 11, 16), count = 60)
        val pages = dates.chunked(25)
        val engine = MockEngine { request ->
            val page = request.url.parameters["page"]!!.toInt()
            respondJson(ApodApiFixtures.page(pages[page - 1]), totalPages = pages.size)
        }

        val items = dataSource(engine)
            .findPODitems(from = "2026-09-18", to = "2026-11-16")
            .getOrNull()

        assertNotNull(items)
        assertEquals(3, engine.requestHistory.size)
        assertEquals(dates, items.map { it.date })
        assertEquals(60, items.map { it.id }.toSet().size)
        assertTrue(items.all { it.url == ApodApiFixtures.imageUrl(it.date) })
        assertEquals("Text for 2026-11-16.", items.first().description)
    }

    @Test
    fun aMissingPictureIsReportedAsAClientError() = runTest {
        val engine = MockEngine {
            respondJson(ApodApiFixtures.NOT_FOUND, status = HttpStatusCode.NotFound)
        }

        val error = dataSource(engine).findPODDay("2027-01-01").leftOrNull()

        assertEquals(400, assertIs<Error.Server>(error).code)
    }

    @Test
    fun anUnavailableServerIsReportedAsAServerError() = runTest {
        val engine = MockEngine {
            respondJson("upstream connect error", status = HttpStatusCode.ServiceUnavailable)
        }

        val error = dataSource(engine)
            .findPODitems(from = "2026-09-18", to = "2026-11-16")
            .leftOrNull()

        assertEquals(500, assertIs<Error.Server>(error).code)
    }

    @Test
    fun aBrokenConnectionIsReportedAsConnectivityError() = runTest {
        val engine = MockEngine { throw IOException("connection reset") }

        val error = dataSource(engine).findPODDay("2026-11-10").leftOrNull()

        assertSame(Error.Connectivity, error)
    }

    @Test
    fun anAnswerThatIsNotJsonIsReportedAsUnknownError() = runTest {
        val engine = MockEngine { respondJson("<html>maintenance</html>") }

        val error = dataSource(engine).findPODDay("2026-11-10").leftOrNull()

        assertIs<Error.Unknown>(error)
    }

    private fun dataSource(engine: MockEngine) = PODServerDataSource(serviceBackedBy(engine))
}
