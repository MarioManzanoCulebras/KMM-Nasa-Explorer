package com.mariomanzano.kmm_nasa_explorer.network

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.network.entities.ApiAPOD
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakeDailyPicturesService
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class PODServerDataSourceTest {

    private val service = FakeDailyPicturesService()
    private val dataSource = PODServerDataSource(service)

    @Test
    fun findPODDayAsksTheServiceForThatDateAndMapsTheAnswer() = runTest {
        service.pictureOfTheDay = { date -> apiPicture(date, title = "Orion") }

        val item = dataSource.findPODDay("2024-01-16").getOrNull()

        assertEquals(listOf("2024-01-16"), service.dayRequests)
        assertNotNull(item)
        assertEquals("2024-01-16", item.date)
        assertEquals("Orion", item.title)
    }

    @Test
    fun findPODDayTurnsAConnectivityFailureIntoAnError() = runTest {
        service.pictureOfTheDay = { throw IOException("offline") }

        val error = dataSource.findPODDay("2024-01-16").leftOrNull()

        assertSame(Error.Connectivity, error)
    }

    @Test
    fun findPODDayTurnsAnUnparseableDateIntoAnUnknownError() = runTest {
        service.pictureOfTheDay = { apiPicture(date = "not-a-date") }

        val error = dataSource.findPODDay("2024-01-16").leftOrNull()

        assertIs<Error.Unknown>(error)
    }

    @Test
    fun findPODitemsAsksTheServiceForTheGivenRange() = runTest {
        service.picturesOfDateRange = { _, _ -> emptyList() }

        dataSource.findPODitems(from = "2023-11-18", to = "2024-01-16")

        assertEquals(listOf("2023-11-18" to "2024-01-16"), service.rangeRequests)
    }

    @Test
    fun findPODitemsReturnsTheItemsNewestFirst() = runTest {
        service.picturesOfDateRange = { _, _ ->
            listOf(
                apiPicture("2024-01-14"),
                apiPicture("2024-01-16"),
                apiPicture("2023-12-31"),
                apiPicture("2024-01-15")
            )
        }

        val items = dataSource.findPODitems(from = "2023-12-31", to = "2024-01-16").getOrNull()

        assertEquals(
            listOf("2024-01-16", "2024-01-15", "2024-01-14", "2023-12-31"),
            items?.map { it.date }
        )
    }

    @Test
    fun findPODitemsReturnsAnEmptyListWhenTheServiceHasNothing() = runTest {
        service.picturesOfDateRange = { _, _ -> emptyList() }

        val items = dataSource.findPODitems(from = "2024-01-01", to = "2024-01-02").getOrNull()

        assertEquals(emptyList(), items)
    }

    @Test
    fun findPODitemsTurnsAServiceFailureIntoAnError() = runTest {
        service.picturesOfDateRange = { _, _ -> throw IllegalStateException("bad json") }

        val error = dataSource.findPODitems(from = "2024-01-01", to = "2024-01-02").leftOrNull()

        assertEquals("bad json", assertIs<Error.Unknown>(error).message)
    }

    @Test
    fun withoutArgumentsTheRangeCoversTheLastSixtyDaysUpToToday() = runTest {
        service.picturesOfDateRange = { _, _ -> emptyList() }

        dataSource.findPODitems()

        val (from, to) = service.rangeRequests.single()
        assertEquals(59, LocalDate.parse(from).daysUntil(LocalDate.parse(to)))
    }

    private fun apiPicture(date: String, title: String? = "Title $date") = ApiAPOD(
        date = date,
        title = title,
        explanation = "Explanation $date",
        media_type = "image",
        url = "https://example.test/$date.jpg"
    )
}
