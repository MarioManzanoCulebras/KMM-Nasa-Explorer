package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import arrow.core.left
import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.LegacyRows
import com.mariomanzano.kmm_nasa_explorer.testdoubles.pictureOfDay
import com.mariomanzano.kmm_nasa_explorer.usecases.GetPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RepairStoredPicturesUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODListUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODSingleDayUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DailyPictureViewModelTest {

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val yesterday: LocalDate = today.minus(1, DateTimeUnit.DAY)
    private val twoDaysAgo: LocalDate = today.minus(2, DateTimeUnit.DAY)

    private val remote = FakePODRemoteDataSource()
    private val viewModels = mutableListOf<DailyPictureViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        viewModels.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    @Test
    fun stateIsEmptyUntilTheStoredPicturesHaveBeenRead() = runTest {
        val viewModel = viewModel(stored = listOf(picture(today)))

        // Nothing has run yet: the initial coroutine is still queued.
        assertEquals(DailyPictureViewModel.UiState(), viewModel.state.value)
    }

    @Test
    fun showsTheStoredPicturesNewestFirstWithoutCallingTheApi() = runTest {
        val viewModel = viewModel(
            stored = listOf(picture(twoDaysAgo), picture(today), picture(yesterday))
        )

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(
            listOf(today, yesterday, twoDaysAgo).map { it.toString() },
            state.dailyPictures?.map { it.date }
        )
        assertTrue(remote.listRequests.isEmpty())
        assertTrue(remote.dayRequests.isEmpty())
    }

    @Test
    fun downloadsTheListWhenNothingIsStored() = runTest {
        remote.listResult = listOf(picture(today), picture(yesterday)).right()

        val viewModel = viewModel(stored = emptyList())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, remote.listRequests.size)
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(
            listOf(today, yesterday).map { it.toString() },
            state.dailyPictures?.map { it.date }
        )
    }

    @Test
    fun showsAnErrorWhenNothingIsStoredAndTheDownloadFails() = runTest {
        val viewModel = viewModel(stored = emptyList())

        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertNull(state.dailyPictures)
        assertSame(Error.NoData, state.error)
        assertFalse(state.showRefresh)
    }

    @Test
    fun offersARefreshWhenTheNewestStoredPictureIsNotFromToday() = runTest {
        val viewModel = viewModel(stored = listOf(picture(yesterday), picture(twoDaysAgo)))

        advanceUntilIdle()

        assertTrue(viewModel.state.value.showRefresh)
    }

    @Test
    fun doesNotOfferARefreshWhenTodaysPictureIsStored() = runTest {
        val viewModel = viewModel(stored = listOf(picture(yesterday), picture(today)))

        advanceUntilIdle()

        assertFalse(viewModel.state.value.showRefresh)
    }

    @Test
    fun refreshingDownloadsTodaysPictureAndAddsItOnTop() = runTest {
        val viewModel = viewModel(stored = listOf(picture(yesterday)))
        advanceUntilIdle()
        remote.dayResult = picture(today).right()

        viewModel.launchDayRequest()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(today.toString()), remote.dayRequests)
        assertFalse(state.loading)
        assertNull(state.error)
        assertFalse(state.showRefresh)
        assertEquals(
            listOf(today, yesterday).map { it.toString() },
            state.dailyPictures?.map { it.date }
        )
    }

    @Test
    fun refreshingKeepsTheStoredPicturesAndReportsTheErrorWhenItFails() = runTest {
        val viewModel = viewModel(stored = listOf(picture(yesterday)))
        advanceUntilIdle()

        viewModel.launchDayRequest()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertSame(Error.NoData, state.error)
        assertTrue(state.showRefresh)
        assertEquals(listOf(yesterday.toString()), state.dailyPictures?.map { it.date })
    }

    @Test
    fun sameDayComparesDayMonthAndYear() {
        val date = LocalDate(2024, 2, 29)

        assertTrue(date.sameDay(LocalDate(2024, 2, 29)))
        assertFalse(date.sameDay(LocalDate(2024, 2, 28)))
        assertFalse(date.sameDay(LocalDate(2024, 3, 29)))
        assertFalse(date.sameDay(LocalDate(2023, 2, 28)))
    }

    @Test
    fun picturesStoredFromTheLegacyApiAreRepairedOnStartAndShown() = runTest {
        val legacyFavorite = LegacyRows.legacyImage(yesterday.toString(), favorite = true)
        val placeholder = LegacyRows.placeholder(twoDaysAgo.toString())
        remote.listAnswer = { _, _ ->
            listOf(LegacyRows.fresh(yesterday.toString()), LegacyRows.fresh(twoDaysAgo.toString())).right()
        }

        val viewModel = viewModel(stored = listOf(legacyFavorite, placeholder))
        advanceUntilIdle()

        val pictures = viewModel.state.value.dailyPictures.orEmpty()
        assertEquals(
            listOf(LegacyRows.newImage(yesterday.toString()), LegacyRows.newImage(twoDaysAgo.toString())),
            pictures.map { it.url }
        )
        assertEquals(
            listOf(legacyFavorite.title, "New title $twoDaysAgo"),
            pictures.map { it.title }
        )
        assertEquals(listOf(true, false), pictures.map { it.favorite })
    }

    @Test
    fun storedPicturesStayVisibleWhenTheRepairFails() = runTest {
        val legacy = LegacyRows.legacyImage(yesterday.toString())
        remote.listAnswer = { _, _ -> Error.Connectivity.left() }

        val viewModel = viewModel(stored = listOf(legacy))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(legacy), state.dailyPictures)
        assertNull(state.error)
    }

    private fun picture(date: LocalDate): PictureOfDayItem = pictureOfDay(date.toString())

    private fun viewModel(stored: List<PictureOfDayItem>): DailyPictureViewModel {
        val repository = DailyPicturesRepository(FakePODLocalDataSource(stored), remote)
        return DailyPictureViewModel(
            getPODUseCase = GetPODUseCase(repository),
            requestPODListUseCase = RequestPODListUseCase(repository),
            requestPODSingleDayUseCase = RequestPODSingleDayUseCase(repository),
            repairStoredPicturesUseCase = RepairStoredPicturesUseCase(repository)
        ).also { viewModels += it }
    }
}
