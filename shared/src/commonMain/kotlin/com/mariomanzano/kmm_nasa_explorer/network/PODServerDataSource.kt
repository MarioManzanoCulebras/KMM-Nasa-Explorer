package com.mariomanzano.kmm_nasa_explorer.network

import arrow.core.Either
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.entities.asPictureOfTheDayItem
import com.mariomanzano.kmm_nasa_explorer.utils.DateTime

class PODServerDataSource(
    private val dailyPicturesService: DailyPicturesService
) : PODRemoteDataSource {

    private val dateTime = DateTime()

    override suspend fun findPODDay(date: String): Either<Error, PictureOfDayItem> =
        tryCall {
            dailyPicturesService
                .getPictureOfTheDay(date)
                .asPictureOfTheDayItem()
        }

    override suspend fun findPODitems(
        from: String,
        to: String
    ): Either<Error, List<PictureOfDayItem>> =
        tryCall {
            dailyPicturesService
                .getPicturesOfDateRange(
                    startDate = from,
                    endDate = to
                )
                .map { it.asPictureOfTheDayItem() }
                .sortedByDescending { it.date }
        }
}