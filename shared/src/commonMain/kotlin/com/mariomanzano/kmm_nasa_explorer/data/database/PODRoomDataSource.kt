package com.mariomanzano.kmm_nasa_explorer.data.database

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.datasources.PODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.tryCall
import com.mariomanzano.kmm_nasa_explorer.shared.cache.Database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class PODRoomDataSource(private val database: Database) : PODLocalDataSource {

    override val podList: Flow<List<PictureOfDayItem>> =
        flowOf(database.getAllPOD())

    override val podListFavorite: Flow<List<PictureOfDayItem>> =
        flowOf(database.getAllPOD().filter { it.favorite })

    override fun findPODById(id: Int): Flow<PictureOfDayItem> =
        flowOf(database.findPODById(id))

    override suspend fun savePODFavoriteList(items: List<PictureOfDayItem>): Error? =
        tryCall {
            database.insertPODList(items)
        }.fold(
            ifLeft = { it },
            ifRight = { null }
        )


    override suspend fun savePODList(items: List<PictureOfDayItem>): Error? =
        tryCall {
            database.insertPODList(items)
        }.fold(
            ifLeft = { it },
            ifRight = { null }
        )

    override suspend fun updatePODList(id: Int, favorite: Boolean): Error? =
        tryCall {
            database.updatePOD(id, favorite)
        }.fold(
            ifLeft = { it },
            ifRight = { null }
        )
}