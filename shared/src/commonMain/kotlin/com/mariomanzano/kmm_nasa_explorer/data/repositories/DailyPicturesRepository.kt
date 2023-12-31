package com.mariomanzano.kmm_nasa_explorer.data.repositories

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.datasources.PODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.coroutines.flow.Flow


class DailyPicturesRepository(
    private val localDataSource: PODLocalDataSource,
    private val remoteDataSource: PODRemoteDataSource
) {

    val podList = localDataSource.podList
    fun findById(id: Int): Flow<PictureOfDayItem> = localDataSource.findPODById(id)

    suspend fun requestPODList(): Either<Error, List<PictureOfDayItem>?> {
        val items = remoteDataSource.findPODitems().getOrNull()
        return if (items.isNullOrEmpty()) {
            Error.NoData.left()
        } else {
            localDataSource.savePODList(items)
            items.right()
        }
    }

    suspend fun switchFavorite(pictureOfDayItem: PictureOfDayItem): Error? {
        val updatedPOD = pictureOfDayItem.copy(favorite = !pictureOfDayItem.favorite)
        return localDataSource.savePODFavoriteList(listOf(updatedPOD))
    }
}