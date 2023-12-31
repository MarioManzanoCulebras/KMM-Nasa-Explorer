package com.mariomanzano.kmm_nasa_explorer.shared.cache

import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.squareup.sqldelight.db.SqlDriver

class Database(sqlDriver: SqlDriver) {
    private val database = NasaDatabase(sqlDriver)
    private val dbQuery = database.nasaDatabaseQueries

    internal fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllPOD()
        }
    }

    internal fun getAllPOD() = dbQuery.getAllPOD(::mapToDomainModel).executeAsList()

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

    internal fun findPODById(id: Int) =
        dbQuery.findPODById(id.toLong(), ::mapToDomainModel).executeAsOne()

    internal fun getPODCount() = dbQuery.getPODCount()

    internal fun updatePOD(id: Int, favorite: Boolean) = dbQuery.updatePOD(id.toLong(), favorite)

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