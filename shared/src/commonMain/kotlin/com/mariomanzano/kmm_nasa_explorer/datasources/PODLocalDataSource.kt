package com.mariomanzano.kmm_nasa_explorer.datasources

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.coroutines.flow.Flow

interface PODLocalDataSource {
    val podList: Flow<List<PictureOfDayItem>>
    val podListFavorite: Flow<List<PictureOfDayItem>>

    fun findPODById(id: Int): Flow<PictureOfDayItem>
    suspend fun savePODFavoriteList(items: List<PictureOfDayItem>): Error?
    suspend fun savePODList(items: List<PictureOfDayItem>): Error?
    suspend fun updatePODList(id: Int, favorite: Boolean): Error?
}