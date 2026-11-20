package com.mariomanzano.kmm_nasa_explorer.data.repositories

import arrow.core.left
import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.pictureOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DailyPicturesRepositoryTest {

    private val local = FakePODLocalDataSource()
    private val remote = FakePODRemoteDataSource()
    private val repository = DailyPicturesRepository(local, remote)

    @Test
    fun requestPODSingleDayStoresAndReturnsTheDownloadedPicture() = runTest {
        val picture = pictureOfDay("2024-01-16")
        remote.dayResult = picture.right()

        val result = repository.requestPODSingleDay("2024-01-16")

        assertEquals(picture, result.getOrNull())
        assertEquals(listOf("2024-01-16"), remote.dayRequests)
        assertEquals(listOf(picture), local.savedItems)
    }

    @Test
    fun requestPODSingleDayReportsNoDataWhateverTheRemoteErrorWas() = runTest {
        remote.dayResult = Error.Server(500).left()

        val result = repository.requestPODSingleDay("2024-01-16")

        assertSame(Error.NoData, result.leftOrNull())
        assertTrue(local.savedItems.isEmpty())
    }

    @Test
    fun requestPODSingleDayStillSucceedsWhenThePictureCannotBeStored() = runTest {
        val picture = pictureOfDay("2024-01-16")
        remote.dayResult = picture.right()
        local.writeError = Error.Unknown("disk full")

        val result = repository.requestPODSingleDay("2024-01-16")

        assertEquals(picture, result.getOrNull())
    }

    @Test
    fun requestPODListStoresAndReturnsTheDownloadedPictures() = runTest {
        val pictures = listOf(pictureOfDay("2024-01-16"), pictureOfDay("2024-01-15"))
        remote.listResult = pictures.right()

        val result = repository.requestPODList()

        assertEquals(pictures, result.getOrNull())
        assertEquals(listOf(pictures), local.savedLists)
        assertEquals(1, remote.listRequests.size)
    }

    @Test
    fun requestPODListReportsNoDataWhenTheRemoteReturnsAnEmptyList() = runTest {
        remote.listResult = emptyList<PictureOfDayItem>().right()

        val result = repository.requestPODList()

        assertSame(Error.NoData, result.leftOrNull())
        assertTrue(local.savedLists.isEmpty())
    }

    @Test
    fun requestPODListReportsNoDataWhateverTheRemoteErrorWas() = runTest {
        remote.listResult = Error.Connectivity.left()

        val result = repository.requestPODList()

        assertSame(Error.NoData, result.leftOrNull())
        assertTrue(local.savedLists.isEmpty())
    }

    @Test
    fun switchFavoriteMarksANonFavoritePictureAsFavorite() = runTest {
        val picture = pictureOfDay("2024-01-16", id = 162024, favorite = false)

        val error = repository.switchFavorite(picture)

        assertNull(error)
        assertEquals(listOf(162024 to true), local.favoriteUpdates)
    }

    @Test
    fun switchFavoriteRemovesTheFavoriteMarkFromAFavoritePicture() = runTest {
        val picture = pictureOfDay("2024-01-16", id = 162024, favorite = true)

        repository.switchFavorite(picture)

        assertEquals(listOf(162024 to false), local.favoriteUpdates)
    }

    @Test
    fun switchFavoriteReturnsTheStorageError() = runTest {
        val storageError = Error.Unknown("locked")
        local.writeError = storageError

        val error = repository.switchFavorite(pictureOfDay("2024-01-16"))

        assertSame(storageError, error)
    }

    @Test
    fun savePODDelegatesToTheLocalDataSource() = runTest {
        val picture = pictureOfDay("2024-01-16")

        val error = repository.savePOD(picture)

        assertNull(error)
        assertEquals(listOf(picture), local.savedItems)
    }

    @Test
    fun exposesTheStoredPicturesAndFavorites() = runTest {
        val plain = pictureOfDay("2024-01-15")
        val favorite = pictureOfDay("2024-01-16", favorite = true)
        val repository = DailyPicturesRepository(
            FakePODLocalDataSource(listOf(plain, favorite)),
            remote
        )

        assertEquals(listOf(plain, favorite), repository.podList.first())
        assertEquals(listOf(favorite), repository.podListFavorite.first())
    }

    @Test
    fun findsAStoredPictureByIdAndByIdAndType() = runTest {
        val picture = pictureOfDay("2024-01-16", id = 162024)
        val repository = DailyPicturesRepository(FakePODLocalDataSource(listOf(picture)), remote)

        assertEquals(picture, repository.findById(162024).first())
        assertEquals(picture, repository.findByIdAndType(162024, "dailyPicture").first())
    }
}
