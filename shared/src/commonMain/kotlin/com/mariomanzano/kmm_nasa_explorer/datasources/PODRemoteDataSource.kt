package com.mariomanzano.kmm_nasa_explorer.datasources

import arrow.core.Either
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

interface PODRemoteDataSource {

    suspend fun findPODDay(
        date: String = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    ): Either<Error, PictureOfDayItem>

    suspend fun findPODitems(
        from: String = Clock.System.now().minus(
            59,
            DateTimeUnit.MONTH,
            TimeZone.currentSystemDefault()
        ).toLocalDateTime(
            TimeZone.currentSystemDefault()
        ).date.toString(),
        to: String = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    ): Either<Error, List<PictureOfDayItem>>
}