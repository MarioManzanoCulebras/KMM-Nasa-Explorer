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
}