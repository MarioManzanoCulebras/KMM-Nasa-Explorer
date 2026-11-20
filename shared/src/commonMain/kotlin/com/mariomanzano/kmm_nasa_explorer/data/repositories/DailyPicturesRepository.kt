package com.mariomanzano.kmm_nasa_explorer.data.repositories

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.data.repairWindows
import com.mariomanzano.kmm_nasa_explorer.data.repairedFrom
import com.mariomanzano.kmm_nasa_explorer.datasources.PODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.APOD_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate


class DailyPicturesRepository(
    private val localDataSource: PODLocalDataSource,
    private val remoteDataSource: PODRemoteDataSource
) {

    val podList = localDataSource.podList
    val podListFavorite = localDataSource.podListFavorite
    fun findById(id: Int): Flow<PictureOfDayItem> = localDataSource.findPODById(id)

    fun findByIdAndType(id: Int, type: String): Flow<PictureOfDayItem> =
        localDataSource.findByIdAndType(id, type)

    suspend fun requestPODSingleDay(date: String): Either<Error, PictureOfDayItem?> {
        val item = remoteDataSource.findPODDay(date).getOrNull()
        return if (item != null) {
            localDataSource.savePOD(item)
            item.right()
        } else {
            Error.NoData.left()
        }
    }

    suspend fun requestPODList(): Either<Error, List<PictureOfDayItem>?> {
        val items = remoteDataSource.findPODitems().getOrNull()
        return if (items.isNullOrEmpty()) {
            Error.NoData.left()
        } else {
            localDataSource.savePODList(items)
            items.right()
        }
    }

    suspend fun savePOD(pictureOfDayItem: PictureOfDayItem): Error? {
        return localDataSource.savePOD(pictureOfDayItem)
    }

    suspend fun switchFavorite(pictureOfDayItem: PictureOfDayItem): Error? {
        val updatedPOD = pictureOfDayItem.copy(favorite = !pictureOfDayItem.favorite)
        return localDataSource.updatePOD(updatedPOD.id, updatedPOD.favorite)
    }

    /**
     * Fixes the stored rows saved from the legacy API (see LegacyContent.kt) with what the current
     * API returns for their dates. Favorites go first, newest first. Every requested date range is
     * saved in one go, so the screens refresh while the repair goes on.
     *
     * Stops at the first network or storage error and returns it: the rows left are tried again
     * the next time. Rows the API has no usable content for are left as they are.
     */
    suspend fun repairStoredPictures(): Error? {
        val pending = localDataSource.findPODsNeedingRepair()
            .mapNotNull { item -> item.localDate()?.let { date -> StoredRow(item, date) } }
            .toMutableList()
        if (pending.isEmpty()) return null

        val (favorites, others) = pending.partition { it.item.favorite }
        val favoriteWindows = repairWindows(favorites.map { it.date }, REPAIR_WINDOW_DAYS)
        val otherDates = others.map { it.date }.filter { date -> favoriteWindows.none { date in it } }
        val windows = favoriteWindows + repairWindows(otherDates, REPAIR_WINDOW_DAYS)

        for (window in windows) {
            val rows = pending.filter { it.date in window }
            if (rows.isEmpty()) continue
            pending.removeAll(rows)

            val fresh = when (
                val result = remoteDataSource.findPODitems(
                    from = window.start.toString(),
                    to = window.endInclusive.toString()
                )
            ) {
                is Either.Left -> return result.value
                is Either.Right -> result.value.associateBy { it.date }
            }
            val repaired = rows.mapNotNull { row ->
                fresh[row.item.date]?.let { repairedFrom(row.item, it) }
            }
            if (repaired.isNotEmpty()) {
                localDataSource.repairPODs(repaired)?.let { return it }
            }
        }
        return null
    }

    private class StoredRow(val item: PictureOfDayItem, val date: LocalDate)

    private fun PictureOfDayItem.localDate(): LocalDate? =
        try {
            LocalDate.parse(date)
        } catch (e: IllegalArgumentException) {
            null
        }

    private companion object {
        /** One page of the API, so that every date range costs a single request. */
        const val REPAIR_WINDOW_DAYS = APOD_PAGE_SIZE
    }
}