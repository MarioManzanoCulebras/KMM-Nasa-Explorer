package com.mariomanzano.kmm_nasa_explorer.usecases

import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.pictureOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class UseCasesTest {

    private val plain = pictureOfDay("2024-01-15", id = 152024)
    private val favorite = pictureOfDay("2024-01-16", id = 162024, favorite = true)

    private val local = FakePODLocalDataSource(listOf(plain, favorite))
    private val remote = FakePODRemoteDataSource()
    private val repository = DailyPicturesRepository(local, remote)

    @Test
    fun getPODReturnsEveryStoredPicture() = runTest {
        assertEquals(listOf(plain, favorite), GetPODUseCase(repository)().first())
    }

    @Test
    fun getFavoritesReturnsOnlyTheFavoritePictures() = runTest {
        assertEquals(listOf(favorite), GetFavoritesUseCase(repository)().first())
    }

    @Test
    fun findPODReturnsThePictureWithThatId() = runTest {
        assertEquals(plain, FindPODUseCase(repository)(152024).first())
    }

    @Test
    fun findFavoriteReturnsThePictureWithThatIdAndType() = runTest {
        assertEquals(favorite, FindFavoriteUseCase(repository)(162024, "dailyPicture").first())
    }

    @Test
    fun requestPODListDownloadsAndStoresTheList() = runTest {
        val downloaded = listOf(pictureOfDay("2024-01-18"), pictureOfDay("2024-01-17"))
        remote.listResult = downloaded.right()

        val result = RequestPODListUseCase(repository)()

        assertEquals(downloaded, result.getOrNull())
        assertEquals(listOf(downloaded), local.savedLists)
    }

    @Test
    fun requestPODListReportsNoDataWhenNothingCouldBeDownloaded() = runTest {
        val result = RequestPODListUseCase(repository)()

        assertSame(Error.NoData, result.leftOrNull())
    }

    @Test
    fun requestPODSingleDayDownloadsTheGivenDate() = runTest {
        val downloaded = pictureOfDay("2024-01-17")
        remote.dayResult = downloaded.right()

        val result = RequestPODSingleDayUseCase(repository)("2024-01-17")

        assertEquals(downloaded, result.getOrNull())
        assertEquals(listOf("2024-01-17"), remote.dayRequests)
    }

    @Test
    fun requestPODSingleDayDefaultsToToday() = runTest {
        val before = today()

        RequestPODSingleDayUseCase(repository)()

        val requested = remote.dayRequests.single()
        // Tolerates the clock passing midnight while the test runs.
        assertEquals(true, requested == before || requested == today())
    }

    @Test
    fun switchItemToFavoriteTogglesTheFavoriteMark() = runTest {
        val error = SwitchItemToFavoriteUseCase(repository)(plain)

        assertNull(error)
        assertEquals(listOf(152024 to true), local.favoriteUpdates)
    }

    private fun today(): String =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}
