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
                .getPictureOfTheDay(dateTime.getFormattedDate(date, "dd.MM.yyyy"))
                .asPictureOfTheDayItem()
        }

    override suspend fun findPODitems(
        from: String,
        to: String
    ): Either<Error, List<PictureOfDayItem>> =
        tryCall {
            dailyPicturesService
                .getPicturesOfDateRange(
                    startDate = dateTime.getFormattedDate(from, "dd.MM.yyyy"),
                    endDate = dateTime.getFormattedDate(to, "dd.MM.yyyy")
                )
                .map { it.asPictureOfTheDayItem() }
                .sortedByDescending { it.date }
        }
}