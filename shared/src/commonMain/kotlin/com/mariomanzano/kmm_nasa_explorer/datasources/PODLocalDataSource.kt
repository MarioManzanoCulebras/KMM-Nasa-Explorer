package com.mariomanzano.kmm_nasa_explorer.datasources

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.coroutines.flow.Flow

interface PODLocalDataSource {
    val podList: Flow<List<PictureOfDayItem>>
    val podListFavorite: Flow<List<PictureOfDayItem>>

    fun findPODById(id: Int): Flow<PictureOfDayItem>

    fun findByIdAndType(id: Int, type: String): Flow<PictureOfDayItem>
    suspend fun savePODFavoriteList(items: List<PictureOfDayItem>): Error?
    suspend fun savePODList(items: List<PictureOfDayItem>): Error?
    suspend fun savePOD(item: PictureOfDayItem): Error?

    suspend fun updatePOD(id: Int, favorite: Boolean): Error?
}