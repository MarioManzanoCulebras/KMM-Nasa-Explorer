package com.mariomanzano.kmm_nasa_explorer.testdoubles

import arrow.core.Either
import arrow.core.left
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.datasources.PODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesService
import com.mariomanzano.kmm_nasa_explorer.network.entities.ApiAPOD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

fun pictureOfDay(
    date: String,
    id: Int = date.filter { it.isDigit() }.toInt(),
    title: String = "Title $date",
    description: String = "Description $date",
    url: String = "https://example.test/$date.jpg",
    mediaType: String = "image",
    favorite: Boolean = false
) = PictureOfDayItem(
    id = id,
    date = date,
    title = title,
    description = description,
    url = url,
    mediaType = mediaType,
    favorite = favorite
)

/** Records every call and answers from the configurable lambdas. */
class FakeDailyPicturesService(
    var pictureOfTheDay: (date: String) -> ApiAPOD = { error("getPictureOfTheDay not stubbed") },
    var picturesOfDateRange: (startDate: String, endDate: String) -> List<ApiAPOD> =
        { _, _ -> error("getPicturesOfDateRange not stubbed") }
) : DailyPicturesService {

    val dayRequests = mutableListOf<String>()
    val rangeRequests = mutableListOf<Pair<String, String>>()

    override suspend fun getPictureOfTheDay(date: String): ApiAPOD {
        dayRequests += date
        return pictureOfTheDay(date)
    }

    override suspend fun getPicturesOfDateRange(startDate: String, endDate: String): List<ApiAPOD> {
        rangeRequests += startDate to endDate
        return picturesOfDateRange(startDate, endDate)
    }
}

class FakePODRemoteDataSource(
    var dayResult: Either<Error, PictureOfDayItem> = Error.NoData.left(),
    var listResult: Either<Error, List<PictureOfDayItem>> = Error.NoData.left()
) : PODRemoteDataSource {

    val dayRequests = mutableListOf<String>()
    val listRequests = mutableListOf<Pair<String, String>>()

    override suspend fun findPODDay(date: String): Either<Error, PictureOfDayItem> {
        dayRequests += date
        return dayResult
    }

    override suspend fun findPODitems(
        from: String,
        to: String
    ): Either<Error, List<PictureOfDayItem>> {
        listRequests += from to to
        return listResult
    }
}

/** In-memory stand-in for the database: writes are reflected in the exposed flows. */
class FakePODLocalDataSource(
    initialItems: List<PictureOfDayItem> = emptyList()
) : PODLocalDataSource {

    private val items = MutableStateFlow(initialItems)

    /** When set, every write fails with this error and stores nothing. */
    var writeError: Error? = null

    val savedLists = mutableListOf<List<PictureOfDayItem>>()
    val savedItems = mutableListOf<PictureOfDayItem>()
    val favoriteUpdates = mutableListOf<Pair<Int, Boolean>>()

    val storedItems: List<PictureOfDayItem> get() = items.value

    override val podList: Flow<List<PictureOfDayItem>> = items

    override val podListFavorite: Flow<List<PictureOfDayItem>> =
        items.map { list -> list.filter { it.favorite } }

    override fun findPODById(id: Int): Flow<PictureOfDayItem> =
        items.mapNotNull { list -> list.firstOrNull { it.id == id } }

    override fun findByIdAndType(id: Int, type: String): Flow<PictureOfDayItem> =
        items.mapNotNull { list -> list.firstOrNull { it.id == id && it.type == type } }

    override suspend fun savePODFavoriteList(items: List<PictureOfDayItem>): Error? =
        savePODList(items)

    override suspend fun savePODList(items: List<PictureOfDayItem>): Error? {
        savedLists += items
        writeError?.let { return it }
        this.items.update { it + items }
        return null
    }

    override suspend fun savePOD(item: PictureOfDayItem): Error? {
        savedItems += item
        writeError?.let { return it }
        items.update { current -> current.filterNot { it.id == item.id } + item }
        return null
    }

    override suspend fun updatePOD(id: Int, favorite: Boolean): Error? {
        favoriteUpdates += id to favorite
        writeError?.let { return it }
        items.update { current ->
            current.map { if (it.id == id) it.copy(favorite = favorite) else it }
        }
        return null
    }
}
