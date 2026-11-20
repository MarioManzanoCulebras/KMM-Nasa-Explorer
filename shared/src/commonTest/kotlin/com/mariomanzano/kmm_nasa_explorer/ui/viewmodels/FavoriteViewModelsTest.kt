package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.FakePODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.testdoubles.pictureOfDay
import com.mariomanzano.kmm_nasa_explorer.usecases.FindFavoriteUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.FindPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.GetFavoritesUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.SwitchItemToFavoriteUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelsTest {

    private val older = pictureOfDay("2024-01-14", id = 142024, favorite = true)
    private val newer = pictureOfDay("2024-01-16", id = 162024, favorite = true)
    private val notFavorite = pictureOfDay("2024-01-15", id = 152024, favorite = false)

    private val viewModels = mutableListOf<ViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        viewModels.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    // region FavoriteViewModel

    @Test
    fun favoritesAreNotLoadedUntilRequested() = runTest {
        val viewModel = favoriteViewModel(local(older, newer))

        advanceUntilIdle()

        assertEquals(FavoriteViewModel.UiState(), viewModel.state.value)
    }

    @Test
    fun getFavoritesShowsOnlyFavoritesNewestFirst() = runTest {
        val viewModel = favoriteViewModel(local(older, notFavorite, newer))

        viewModel.getFavorites()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertNull(state.error)
        assertEquals(listOf(newer, older), state.items)
    }

    @Test
    fun favoritesAreNotDuplicatedWhenTheDatabaseChanges() = runTest {
        val local = local(older, newer)
        val viewModel = favoriteViewModel(local)
        viewModel.getFavorites()
        advanceUntilIdle()

        local.repairPODs(listOf(newer.copy(url = "https://assets.science.nasa.gov/new.jpg")))
        advanceUntilIdle()

        assertEquals(
            listOf(newer.copy(url = "https://assets.science.nasa.gov/new.jpg"), older),
            viewModel.state.value.items
        )
    }

    @Test
    fun openingFavoritesAgainDoesNotDuplicateThem() = runTest {
        val local = local(older, notFavorite, newer)
        val viewModel = favoriteViewModel(local)

        viewModel.getFavorites()
        advanceUntilIdle()
        viewModel.getFavorites()
        advanceUntilIdle()
        // Any change in the database emits the favorites again.
        local.repairPODs(listOf(notFavorite.copy(title = "Changed")))
        advanceUntilIdle()

        assertEquals(listOf(newer, older), viewModel.state.value.items)
    }

    @Test
    fun getFavoritesReportsNoDataWhenThereAreNoFavorites() = runTest {
        val viewModel = favoriteViewModel(local(notFavorite))

        viewModel.getFavorites()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertSame(Error.NoData, state.error)
        assertTrue(state.items.isNullOrEmpty())
    }

    // endregion

    // region FavoriteDetailViewModel

    @Test
    fun favoriteDetailShowsTheRequestedItem() = runTest {
        val viewModel = favoriteDetailViewModel(local(older, newer), itemId = newer.id)

        advanceUntilIdle()

        assertEquals(newer, viewModel.state.value.nasaItem)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun favoriteDetailStaysEmptyWhileTheItemIsUnknown() = runTest {
        val viewModel = favoriteDetailViewModel(local(older), itemId = 999)

        advanceUntilIdle()

        assertNull(viewModel.state.value.nasaItem)
    }

    @Test
    fun favoriteDetailTogglesTheFavoriteMarkAndShowsTheUpdatedItem() = runTest {
        val local = local(older, newer)
        val viewModel = favoriteDetailViewModel(local, itemId = newer.id)
        advanceUntilIdle()

        viewModel.onFavoriteClicked()
        advanceUntilIdle()

        assertEquals(listOf(newer.id to false), local.favoriteUpdates)
        assertEquals(false, viewModel.state.value.nasaItem?.favorite)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun favoriteDetailReportsTheErrorWhenTheToggleCannotBeStored() = runTest {
        val storageError = Error.Unknown("locked")
        val local = local(newer)
        val viewModel = favoriteDetailViewModel(local, itemId = newer.id)
        advanceUntilIdle()
        local.writeError = storageError

        viewModel.onFavoriteClicked()
        advanceUntilIdle()

        assertSame(storageError, viewModel.state.value.error)
        assertEquals(true, viewModel.state.value.nasaItem?.favorite)
    }

    @Test
    fun favoriteDetailIgnoresTheClickWhileNoItemIsLoaded() = runTest {
        val local = local(older)
        val viewModel = favoriteDetailViewModel(local, itemId = 999)
        advanceUntilIdle()

        viewModel.onFavoriteClicked()
        advanceUntilIdle()

        assertTrue(local.favoriteUpdates.isEmpty())
    }

    // endregion

    // region DailyPictureDetailViewModel

    @Test
    fun dailyPictureDetailShowsTheRequestedPicture() = runTest {
        val viewModel = dailyPictureDetailViewModel(local(notFavorite, newer), notFavorite.id)

        advanceUntilIdle()

        assertEquals(notFavorite, viewModel.state.value.dailyPicture)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun dailyPictureDetailTogglesTheFavoriteMarkAndShowsTheUpdatedPicture() = runTest {
        val local = local(notFavorite)
        val viewModel = dailyPictureDetailViewModel(local, notFavorite.id)
        advanceUntilIdle()

        viewModel.onFavoriteClicked()
        advanceUntilIdle()

        assertEquals(listOf(notFavorite.id to true), local.favoriteUpdates)
        assertEquals(true, viewModel.state.value.dailyPicture?.favorite)
    }

    @Test
    fun dailyPictureDetailReportsTheErrorWhenTheToggleCannotBeStored() = runTest {
        val storageError = Error.Unknown("locked")
        val local = local(notFavorite)
        val viewModel = dailyPictureDetailViewModel(local, notFavorite.id)
        advanceUntilIdle()
        local.writeError = storageError

        viewModel.onFavoriteClicked()
        advanceUntilIdle()

        assertSame(storageError, viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.dailyPicture?.favorite)
    }

    // endregion

    private fun local(vararg items: PictureOfDayItem) = FakePODLocalDataSource(items.toList())

    private fun repository(local: FakePODLocalDataSource) =
        DailyPicturesRepository(local, FakePODRemoteDataSource())

    private fun favoriteViewModel(local: FakePODLocalDataSource) =
        FavoriteViewModel(GetFavoritesUseCase(repository(local))).also { viewModels += it }

    private fun favoriteDetailViewModel(
        local: FakePODLocalDataSource,
        itemId: Int
    ): FavoriteDetailViewModel {
        val repository = repository(local)
        return FavoriteDetailViewModel(
            itemId = itemId,
            type = "dailyPicture",
            findFavoriteUseCase = FindFavoriteUseCase(repository),
            switchFavoriteUseCase = SwitchItemToFavoriteUseCase(repository)
        ).also { viewModels += it }
    }

    private fun dailyPictureDetailViewModel(
        local: FakePODLocalDataSource,
        itemId: Int
    ): DailyPictureDetailViewModel {
        val repository = repository(local)
        return DailyPictureDetailViewModel(
            itemId = itemId,
            findPODUseCase = FindPODUseCase(repository),
            favoriteUseCase = SwitchItemToFavoriteUseCase(repository)
        ).also { viewModels += it }
    }
}
