package com.mariomanzano.kmm_nasa_explorer.data.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.datasources.PODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.tryCall
import com.mariomanzano.kmm_nasa_explorer.shared.cache.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class PODRoomDataSource(private val database: Database) : PODLocalDataSource {

    override val podList: Flow<List<PictureOfDayItem>> =
        database.getAllPOD().asFlow().mapToList(Dispatchers.Default)

    override val podListFavorite: Flow<List<PictureOfDayItem>> =
        database.getAllFavoritePOD().asFlow().mapToList(Dispatchers.Default)

    override fun findPODById(id: Int): Flow<PictureOfDayItem> =
        database.findPODById(id).asFlow().mapToOne(Dispatchers.Default)

    override suspend fun savePODFavoriteList(items: List<PictureOfDayItem>): Error? =
        tryCall {
            database.insertPODList(items)
        }.fold(
            ifLeft = { it },
            ifRight = { null }
        )

    override suspend fun updatePOD(id: Int, favorite: Boolean): Error? =
        tryCall {
            database.updatePOD(id, favorite)
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

    override suspend fun savePOD(item: PictureOfDayItem): Error? =
        tryCall {
            database.replacePOD(item)
        }.fold(
            ifLeft = { it },
            ifRight = { null }
        )
}