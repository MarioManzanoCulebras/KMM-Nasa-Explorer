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

    private fun picture(id: Int, date: String) = PictureOfDayItem(
        id = id,
        date = date,
        title = "Title $date",
        description = "Description $date",
        url = "https://example.test/$date.jpg",
        mediaType = "image"
    )
}
