package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import kotlinx.coroutines.flow.Flow

class FindPODUseCase(private val repository: DailyPicturesRepository) {

    operator fun invoke(id: Int): Flow<PictureOfDayItem> = repository.findById(id)
}