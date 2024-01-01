package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RequestPODSingleDayUseCase(private val repository: DailyPicturesRepository) {

    suspend operator fun invoke(
        date: String = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    ) =
        repository.requestPODSingleDay(date)
}