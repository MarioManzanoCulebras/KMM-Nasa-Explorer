package com.mariomanzano.kmm_nasa_explorer.shared.cache

import app.cash.sqldelight.db.SqlDriver
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem

class Database(sqlDriver: SqlDriver) {
    private val database = NasaDatabase(sqlDriver)
    private val dbQuery = database.nasaDatabaseQueries

    internal fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllPOD()
        }
    }

    internal fun getAllPOD() = dbQuery.getAllPOD(::mapToDomainModel)

    internal fun getAllFavoritePOD() = dbQuery.getAllFavoritePOD(::mapToDomainModel)

    internal fun insertPODList(list: List<PictureOfDayItem>) {
        dbQuery.transaction {
            list.forEach {
                insertPOD(it)
            }
        }
    }

    internal fun insertPOD(pod: PictureOfDayItem) {
        dbQuery.insertPODOnDb(
            id = pod.id.toLong(),
            date = pod.date,
            title = pod.title,
            description = pod.description,
            url = pod.url,
            mediaType = pod.mediaType,
            favorite = pod.favorite,
            type = pod.type
        )
    }

    internal fun replacePOD(pod: PictureOfDayItem) {
        dbQuery.replacePOD(
            id = pod.id.toLong(),
            date = pod.date,
            title = pod.title,
            description = pod.description,
            url = pod.url,
            mediaType = pod.mediaType,
            favorite = pod.favorite,
            type = pod.type
        )
    }

    internal fun findPODById(id: Int) =
        dbQuery.findPODById(id.toLong(), ::mapToDomainModel)

    internal fun getPODCount() = dbQuery.getPODCount()

    internal fun updatePOD(id: Int, favorite: Boolean) = dbQuery.updatePOD(favorite, id.toLong())

    private fun mapToDomainModel(
        id: Long,
        date: String,
        title: String,
        description: String,
        url: String,
        mediaType: String,
        favorite: Boolean,
        type: String
    ) = PictureOfDayItem(
        id = id.toInt(),
        date = date,
        title = title,
        description = description,
        url = url,
        mediaType = mediaType,
        favorite = favorite
    )
}