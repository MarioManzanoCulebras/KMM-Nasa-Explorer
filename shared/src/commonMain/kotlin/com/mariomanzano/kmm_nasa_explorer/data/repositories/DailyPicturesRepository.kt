package com.mariomanzano.kmm_nasa_explorer.data.repositories

import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem

class DailyPicturesRepository(
    private val remoteDataSource: PODRemoteDataSource
) {

    val localList: List<PictureOfDayItem>? = null

    fun findById(id: Int): PictureOfDayItem? = localList?.find { it.id == id }
    suspend fun requestPODList() = remoteDataSource.findPODitems()
}