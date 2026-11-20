package com.mariomanzano.kmm_nasa_explorer.data.repositories

import arrow.core.left
import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.LegacyRows
import com.mariomanzano.kmm_nasa_explorer.testdoubles.pictureOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RepairStoredPicturesTest {

    private val remote = FakePODRemoteDataSource()

    /** The current API: it knows every date except [missing]. */
    private fun answerEveryDate(missing: Set<String> = emptySet()) {
        remote.listAnswer = { from, to ->
            datesBetween(from, to).filterNot { it in missing }.map { LegacyRows.fresh(it) }.right()
        }
    }

    @Test
    fun nothingIsRequestedWhenNoRowNeedsRepair() = runTest {
        val local = FakePODLocalDataSource(
            listOf(
                pictureOfDay("2026-11-10", url = LegacyRows.newImage("2026-11-10")),
                LegacyRows.legacyVideo("2023-12-03")
            )
        )
        answerEveryDate()

        val error = DailyPicturesRepository(local, remote).repairStoredPictures()

        assertNull(error)
        assertTrue(remote.listRequests.isEmpty())
        assertTrue(local.repairBatches.isEmpty())
    }

    @Test
    fun nothingIsRequestedForAnEmptyDatabase() = runTest {
        val local = FakePODLocalDataSource()

        DailyPicturesRepository(local, remote).repairStoredPictures()

        assertTrue(remote.listRequests.isEmpty())
    }

    @Test
    fun legacyImagesGetTheNewImageAndKeepTheirTextAndFavorite() = runTest {
        val favorite = LegacyRows.legacyImage("2024-01-16", favorite = true)
        val other = LegacyRows.legacyImage("2024-01-15")
        val local = FakePODLocalDataSource(listOf(favorite, other))
        answerEveryDate()

        val error = DailyPicturesRepository(local, remote).repairStoredPictures()

        assertNull(error)
        assertEquals(
            setOf(
                favorite.copy(url = LegacyRows.newImage("2024-01-16")),
                other.copy(url = LegacyRows.newImage("2024-01-15"))
            ),
            local.storedItems.toSet()
        )
    }

    @Test
    fun placeholderRowsGetTheirRealContent() = runTest {
        val placeholder = LegacyRows.placeholder("2026-09-20", favorite = true)
        val local = FakePODLocalDataSource(listOf(placeholder))
        answerEveryDate()

        DailyPicturesRepository(local, remote).repairStoredPictures()

        val fresh = LegacyRows.fresh("2026-09-20")
        assertEquals(
            listOf(
                placeholder.copy(
                    title = fresh.title,
                    description = fresh.description,
                    url = fresh.url,
                    mediaType = fresh.mediaType
                )
            ),
            local.storedItems
        )
    }

    @Test
    fun videosAndCurrentRowsAreLeftUntouched() = runTest {
        val video = LegacyRows.legacyVideo("2024-01-14")
        val current = pictureOfDay("2024-01-13", url = LegacyRows.newImage("2024-01-13"))
        val local = FakePODLocalDataSource(listOf(video, current, LegacyRows.legacyImage("2024-01-15")))
        answerEveryDate()

        DailyPicturesRepository(local, remote).repairStoredPictures()

        assertTrue(video in local.storedItems)
        assertTrue(current in local.storedItems)
        assertEquals(listOf(listOf("2024-01-15")), local.repairBatches.map { batch -> batch.map { it.date } })
    }

    @Test
    fun favoritesAreRepairedBeforeTheOtherRows() = runTest {
        val local = FakePODLocalDataSource(
            listOf(
                LegacyRows.legacyImage("2026-09-01"),
                LegacyRows.legacyImage("2024-01-16", favorite = true),
                LegacyRows.legacyImage("2025-05-10")
            )
        )
        answerEveryDate()

        DailyPicturesRepository(local, remote).repairStoredPictures()

        assertEquals(
            listOf(
                "2024-01-16" to "2024-01-16",
                "2026-09-01" to "2026-09-01",
                "2025-05-10" to "2025-05-10"
            ),
            remote.listRequests
        )
    }

    @Test
    fun consecutiveDaysAreRequestedAndSavedTwentyFiveAtATime() = runTest {
        val dates = (0 until 60).map { LocalDate(2026, 9, 9).minus(it, DateTimeUnit.DAY).toString() }
        val local = FakePODLocalDataSource(dates.map { LegacyRows.legacyImage(it) })
        answerEveryDate()

        DailyPicturesRepository(local, remote).repairStoredPictures()

        assertEquals(
            listOf("2026-08-16" to "2026-09-09", "2026-07-22" to "2026-08-15", "2026-07-12" to "2026-07-21"),
            remote.listRequests
        )
        assertEquals(listOf(25, 25, 10), local.repairBatches.map { it.size })
        assertTrue(local.storedItems.all { it.url == LegacyRows.newImage(it.date) })
    }

    @Test
    fun otherRowsInsideAFavoriteRangeShareItsRequest() = runTest {
        val local = FakePODLocalDataSource(
            listOf(
                LegacyRows.legacyImage("2024-01-16", favorite = true),
                LegacyRows.legacyImage("2024-01-01", favorite = true),
                LegacyRows.legacyImage("2024-01-10")
            )
        )
        answerEveryDate()

        DailyPicturesRepository(local, remote).repairStoredPictures()

        assertEquals(listOf("2024-01-01" to "2024-01-16"), remote.listRequests)
        assertEquals(1, local.repairBatches.size)
        assertTrue(local.storedItems.all { it.url == LegacyRows.newImage(it.date) })
    }

    @Test
    fun aNetworkErrorStopsTheRepairAndKeepsTheRowsLeftForLater() = runTest {
        val local = FakePODLocalDataSource(
            listOf(LegacyRows.legacyImage("2026-09-01"), LegacyRows.legacyImage("2024-01-16"))
        )
        remote.listAnswer = { from, to ->
            if (from == "2026-09-01") listOf(LegacyRows.fresh(from)).right() else Error.Connectivity.left()
        }

        val error = DailyPicturesRepository(local, remote).repairStoredPictures()

        assertSame(Error.Connectivity, error)
        assertEquals(LegacyRows.newImage("2026-09-01"), local.url("2026-09-01"))
        assertEquals(LegacyRows.legacyImage("2024-01-16").url, local.url("2024-01-16"))
    }

    @Test
    fun aStorageErrorStopsTheRepair() = runTest {
        val local = FakePODLocalDataSource(
            listOf(LegacyRows.legacyImage("2026-09-01"), LegacyRows.legacyImage("2024-01-16"))
        )
        local.writeError = Error.Unknown("disk full")
        answerEveryDate()

        val error = DailyPicturesRepository(local, remote).repairStoredPictures()

        assertIs<Error.Unknown>(error)
        assertEquals(1, remote.listRequests.size)
    }

    @Test
    fun datesTheApiDoesNotKnowStayBrokenAndAreTriedAgainNextTime() = runTest {
        val local = FakePODLocalDataSource(
            listOf(LegacyRows.legacyImage("2024-01-16"), LegacyRows.legacyImage("2024-01-15"))
        )
        answerEveryDate(missing = setOf("2024-01-15"))
        val repository = DailyPicturesRepository(local, remote)

        repository.repairStoredPictures()
        remote.listRequests.clear()
        repository.repairStoredPictures()

        assertEquals(LegacyRows.legacyImage("2024-01-15").url, local.url("2024-01-15"))
        assertEquals(listOf("2024-01-15" to "2024-01-15"), remote.listRequests)
    }

    @Test
    fun aSecondRunAfterACompleteRepairRequestsNothing() = runTest {
        val local = FakePODLocalDataSource(
            listOf(LegacyRows.legacyImage("2024-01-16"), LegacyRows.placeholder("2026-09-20"))
        )
        answerEveryDate()
        val repository = DailyPicturesRepository(local, remote)

        repository.repairStoredPictures()
        remote.listRequests.clear()
        val error = repository.repairStoredPictures()

        assertNull(error)
        assertTrue(remote.listRequests.isEmpty())
    }

    @Test
    fun theStoredListShowsTheRepairedImages() = runTest {
        val local = FakePODLocalDataSource(listOf(LegacyRows.legacyImage("2024-01-16", favorite = true)))
        answerEveryDate()
        val repository = DailyPicturesRepository(local, remote)

        repository.repairStoredPictures()

        assertEquals(LegacyRows.newImage("2024-01-16"), repository.podList.first().single().url)
        assertEquals(LegacyRows.newImage("2024-01-16"), repository.podListFavorite.first().single().url)
    }

    private fun FakePODLocalDataSource.url(date: String): String =
        storedItems.single { it.date == date }.url

    private fun datesBetween(from: String, to: String): List<String> {
        val dates = mutableListOf<String>()
        var date = LocalDate.parse(from)
        val end = LocalDate.parse(to)
        while (date <= end) {
            dates += date.toString()
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return dates
    }
}
