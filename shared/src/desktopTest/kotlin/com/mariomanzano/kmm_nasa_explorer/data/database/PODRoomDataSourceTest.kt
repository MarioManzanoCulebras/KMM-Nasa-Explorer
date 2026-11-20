package com.mariomanzano.kmm_nasa_explorer.data.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.shared.cache.Database
import com.mariomanzano.kmm_nasa_explorer.shared.cache.NasaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs the real SQLDelight queries against an in-memory SQLite database. */
class PODRoomDataSourceTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        NasaDatabase.Schema.create(it)
    }
    private val dataSource = PODRoomDataSource(Database(driver))

    private val monday = picture(id = 152024, date = "2024-01-15")
    private val tuesday = picture(id = 162024, date = "2024-01-16")

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun aNewDatabaseHasNoPictures() = runTest {
        assertTrue(dataSource.podList.first().isEmpty())
        assertTrue(dataSource.podListFavorite.first().isEmpty())
    }

    @Test
    fun savedPicturesAreReadBackWithTheSameContent() = runTest {
        val error = dataSource.savePODList(listOf(monday, tuesday))

        assertNull(error)
        assertEquals(setOf(monday, tuesday), dataSource.podList.first().toSet())
    }

    @Test
    fun savingAListThatContainsAnAlreadyStoredPictureFailsAndStoresNothing() = runTest {
        dataSource.savePODList(listOf(monday))

        val error = dataSource.savePODList(listOf(tuesday, monday))

        assertIs<Error.Unknown>(error)
        assertEquals(listOf(monday), dataSource.podList.first())
    }

    @Test
    fun savePODAddsAPictureThatWasNotStored() = runTest {
        dataSource.savePODList(listOf(monday))

        val error = dataSource.savePOD(tuesday)

        assertNull(error)
        assertEquals(setOf(monday, tuesday), dataSource.podList.first().toSet())
    }

    @Test
    fun savePODReplacesTheContentOfAnAlreadyStoredPicture() = runTest {
        dataSource.savePODList(listOf(monday))
        val updated = monday.copy(title = "New title", url = "https://example.test/new.jpg")

        val error = dataSource.savePOD(updated)

        assertNull(error)
        assertEquals(listOf(updated), dataSource.podList.first())
    }

    @Test
    fun updatePODChangesOnlyTheFavoriteMark() = runTest {
        dataSource.savePODList(listOf(monday, tuesday))

        val error = dataSource.updatePOD(tuesday.id, favorite = true)

        assertNull(error)
        assertEquals(
            setOf(monday, tuesday.copy(favorite = true)),
            dataSource.podList.first().toSet()
        )
    }

    @Test
    fun favoritesListContainsOnlyFavoritePictures() = runTest {
        dataSource.savePODList(listOf(monday, tuesday))
        dataSource.updatePOD(monday.id, favorite = true)

        assertEquals(listOf(monday.copy(favorite = true)), dataSource.podListFavorite.first())
    }

    @Test
    fun aFavoriteMarkCanBeRemovedAgain() = runTest {
        dataSource.savePODList(listOf(monday))
        dataSource.updatePOD(monday.id, favorite = true)

        dataSource.updatePOD(monday.id, favorite = false)

        assertTrue(dataSource.podListFavorite.first().isEmpty())
    }

    @Test
    fun findPODByIdReturnsTheStoredPicture() = runTest {
        dataSource.savePODList(listOf(monday, tuesday))

        assertEquals(tuesday, dataSource.findPODById(tuesday.id).first())
    }

    @Test
    fun findByIdAndTypeReturnsTheStoredPicture() = runTest {
        dataSource.savePODList(listOf(monday, tuesday))

        assertEquals(monday, dataSource.findByIdAndType(monday.id, "dailyPicture").first())
    }

    // region pictures saved from the legacy API

    @Test
    fun findPODsNeedingRepairReturnsLegacyImagesAndPlaceholdersFavoritesFirstNewestFirst() = runTest {
        val oldFavorite = legacyImage(id = 12024, date = "2024-01-01", favorite = true)
        val newFavorite = placeholder(id = 2632026, date = "2026-09-20", favorite = true)
        val oldImage = legacyImage(id = 152024, date = "2024-01-15")
            .copy(url = "http://apod.nasa.gov/apod/image/2401/Old_960.jpg")
        val newPlaceholder = placeholder(id = 2642026, date = "2026-09-21")
        dataSource.savePODList(listOf(oldImage, newPlaceholder, oldFavorite, newFavorite))

        val rows = dataSource.findPODsNeedingRepair()

        assertEquals(listOf(newFavorite, oldFavorite, newPlaceholder, oldImage), rows)
    }

    @Test
    fun findPODsNeedingRepairSkipsVideosAndCurrentPictures() = runTest {
        val video = picture(id = 3372023, date = "2023-12-03").copy(
            url = "https://www.youtube.com/embed/afHfMMC-MJE?rel=0",
            mediaType = "video"
        )
        val legacyHostVideo = legacyImage(id = 3452023, date = "2023-12-11").copy(mediaType = "video")
        val current = picture(id = 3142026, date = "2026-11-10").copy(
            url = "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/2026/november/Saturn.jpg"
        )
        dataSource.savePODList(listOf(video, legacyHostVideo, current))

        assertTrue(dataSource.findPODsNeedingRepair().isEmpty())
    }

    @Test
    fun repairPODsReplacesTheContentAndKeepsTheFavoriteMark() = runTest {
        val favorite = legacyImage(id = 162024, date = "2024-01-16", favorite = true)
        val other = placeholder(id = 2632026, date = "2026-09-20")
        dataSource.savePODList(listOf(favorite, other))
        val repairedFavorite = favorite.copy(url = NEW_IMAGE).copy(favorite = false)
        val repairedOther = other.copy(
            title = "Real title",
            description = "Real description",
            url = "https://science.nasa.gov/image-article/apod-2026-september-20/",
            mediaType = "video"
        )

        val error = dataSource.repairPODs(listOf(repairedFavorite, repairedOther))

        assertNull(error)
        assertEquals(
            setOf(repairedFavorite.copy(favorite = true), repairedOther),
            dataSource.podList.first().toSet()
        )
        assertTrue(dataSource.findPODsNeedingRepair().isEmpty())
    }

    @Test
    fun repairPODsLeavesOtherRowsAlone() = runTest {
        val broken = legacyImage(id = 162024, date = "2024-01-16")
        dataSource.savePODList(listOf(broken, monday))

        dataSource.repairPODs(listOf(broken.copy(url = NEW_IMAGE)))

        assertEquals(setOf(broken.copy(url = NEW_IMAGE), monday), dataSource.podList.first().toSet())
    }

    private fun legacyImage(id: Int, date: String, favorite: Boolean = false) =
        picture(id = id, date = date).copy(
            url = "https://apod.nasa.gov/apod/image/${date.substring(2, 4)}${date.substring(5, 7)}/Picture_960.jpg",
            favorite = favorite
        )

    private fun placeholder(id: Int, date: String, favorite: Boolean = false) =
        picture(id = id, date = date).copy(
            title = "NASA Science",
            url = "https://science.nasa.gov/wp-content/themes/nasa-child/assets/images/nasa-logo@2x.png",
            favorite = favorite
        )

    // endregion

    private companion object {
        const val NEW_IMAGE =
            "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/2024/january/Picture_2048.jpg"
    }

    private fun picture(id: Int, date: String) = PictureOfDayItem(
        id = id,
        date = date,
        title = "Title $date",
        description = "Description $date",
        url = "https://example.test/$date.jpg",
        mediaType = "image"
    )
}
