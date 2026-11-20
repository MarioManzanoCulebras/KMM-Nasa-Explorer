package com.mariomanzano.kmm_nasa_explorer.data

import com.mariomanzano.kmm_nasa_explorer.testdoubles.LegacyRows
import com.mariomanzano.kmm_nasa_explorer.testdoubles.pictureOfDay
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LegacyContentTest {

    // region detection

    @Test
    fun imagesOnTheLegacyHostAreLegacyImageRows() {
        assertTrue(LegacyRows.legacyImage("2024-01-16").isLegacyImageRow())
        assertTrue(
            LegacyRows.legacyImage("2024-01-16")
                .copy(url = "http://apod.nasa.gov/apod/image/2401/OrionMcc_Guzzini_960.jpg")
                .isLegacyImageRow()
        )
    }

    @Test
    fun videosAndNewImagesAreNotLegacyImageRows() {
        assertFalse(LegacyRows.legacyVideo("2023-12-03").isLegacyImageRow())
        assertFalse(pictureOfDay("2026-11-10", url = LegacyRows.newImage("2026-11-10")).isLegacyImageRow())
        assertFalse(LegacyRows.placeholder("2026-09-20").isLegacyImageRow())
    }

    @Test
    fun rowsWithTheNasaLogoArePlaceholderRows() {
        assertTrue(LegacyRows.placeholder("2026-09-20").isPlaceholderRow())
        assertFalse(LegacyRows.legacyImage("2024-01-16").isPlaceholderRow())
    }

    // endregion

    // region repairedFrom

    @Test
    fun legacyImageTakesTheNewImageAndKeepsItsOwnTextAndFavorite() {
        val stored = LegacyRows.legacyImage("2024-01-16", favorite = true)
        val fresh = LegacyRows.fresh("2024-01-16")

        val repaired = repairedFrom(stored, fresh)

        assertEquals(stored.copy(url = fresh.url), repaired)
    }

    @Test
    fun placeholderRowTakesTheWholeContentAndKeepsItsFavorite() {
        val stored = LegacyRows.placeholder("2026-09-20", favorite = true)
        val fresh = LegacyRows.fresh("2026-09-20")

        val repaired = repairedFrom(stored, fresh)

        assertEquals(
            stored.copy(
                title = fresh.title,
                description = fresh.description,
                url = fresh.url,
                mediaType = fresh.mediaType
            ),
            repaired
        )
        assertTrue(repaired!!.favorite)
    }

    @Test
    fun placeholderRowOfAVideoDayBecomesThatVideo() {
        val fresh = LegacyRows.fresh("2026-09-20", mediaType = "video")
            .copy(url = "https://science.nasa.gov/image-article/apod-2026-september-20/")

        val repaired = repairedFrom(LegacyRows.placeholder("2026-09-20"), fresh)

        assertEquals("video", repaired?.mediaType)
        assertEquals(fresh.url, repaired?.url)
    }

    @Test
    fun legacyImageIsNotRepairedWhenTheDayIsNowAVideo() {
        val fresh = LegacyRows.fresh("2024-01-16", mediaType = "video")

        assertNull(repairedFrom(LegacyRows.legacyImage("2024-01-16"), fresh))
    }

    @Test
    fun nothingIsRepairedWithoutAnImageOfTheNewHost() {
        val stored = LegacyRows.legacyImage("2024-01-16")
        val page = "https://science.nasa.gov/image-article/apod-2024-january-16/"
        val genericPicture = "https://assets.science.nasa.gov/dynamicimage/assets/science/astro/" +
                "programs/cosmic-origins/images/misc/news-thumbnail.png?w=594&h=516&fit=clip"

        assertNull(repairedFrom(stored, LegacyRows.fresh("2024-01-16").copy(url = "")))
        assertNull(repairedFrom(stored, LegacyRows.fresh("2024-01-16").copy(url = page)))
        assertNull(repairedFrom(stored, LegacyRows.fresh("2024-01-16").copy(url = genericPicture)))
        assertNull(repairedFrom(LegacyRows.placeholder("2026-09-20"), LegacyRows.fresh("2026-09-20").copy(url = page)))
    }

    @Test
    fun placeholderRowIsNotRepairedWithAnEmptyTitle() {
        val fresh = LegacyRows.fresh("2026-09-20").copy(title = "")

        assertNull(repairedFrom(LegacyRows.placeholder("2026-09-20"), fresh))
    }

    @Test
    fun rowsThatAreNotLegacyAreNeverChanged() {
        val stored = pictureOfDay("2026-11-10", url = LegacyRows.newImage("2026-11-10"))

        assertNull(repairedFrom(stored, LegacyRows.fresh("2026-11-10")))
    }

    // endregion

    // region repairWindows

    @Test
    fun noDatesMeansNoWindows() {
        assertTrue(repairWindows(emptyList(), maxDays = 25).isEmpty())
    }

    @Test
    fun aSingleDateIsASingleDayWindow() {
        val date = LocalDate(2024, 1, 16)

        assertEquals(listOf(date..date), repairWindows(listOf(date), maxDays = 25))
    }

    @Test
    fun consecutiveDaysShareWindowsOfAtMostMaxDays() {
        val dates = (0 until 60).map { LocalDate(2024, 3, 1).plusDays(-it) }

        val windows = repairWindows(dates, maxDays = 25)

        assertEquals(
            listOf(
                LocalDate(2024, 2, 6)..LocalDate(2024, 3, 1),
                LocalDate(2024, 1, 12)..LocalDate(2024, 2, 5),
                LocalDate(2024, 1, 2)..LocalDate(2024, 1, 11)
            ),
            windows
        )
    }

    @Test
    fun datesFarApartGetTheirOwnWindows() {
        val dates = listOf(LocalDate(2026, 9, 1), LocalDate(2025, 5, 10), LocalDate(2024, 1, 16))

        assertEquals(dates.map { it..it }, repairWindows(dates, maxDays = 25))
    }

    @Test
    fun datesWithSmallGapsShareAWindow() {
        val dates = listOf(LocalDate(2024, 1, 30), LocalDate(2024, 1, 20), LocalDate(2024, 1, 6))

        assertEquals(
            listOf(LocalDate(2024, 1, 6)..LocalDate(2024, 1, 30)),
            repairWindows(dates, maxDays = 25)
        )
    }

    @Test
    fun aWindowIsClosedAsSoonAsTheNextDateDoesNotFit() {
        // January 6 to 31 spans 26 days: one too many.
        val dates = listOf(LocalDate(2024, 1, 31), LocalDate(2024, 1, 6), LocalDate(2024, 1, 7))

        assertEquals(
            listOf(
                LocalDate(2024, 1, 31)..LocalDate(2024, 1, 31),
                LocalDate(2024, 1, 6)..LocalDate(2024, 1, 7)
            ),
            repairWindows(dates, maxDays = 25)
        )
    }

    @Test
    fun maxDaysMustBePositive() {
        assertFailsWith<IllegalArgumentException> {
            repairWindows(listOf(LocalDate(2024, 1, 1)), maxDays = 0)
        }
    }

    // endregion

    private fun LocalDate.plusDays(days: Int): LocalDate =
        LocalDate.fromEpochDays(toEpochDays() + days)
}
