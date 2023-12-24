package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem

class FindPODUseCase(private val repository: DailyPicturesRepository) {

    operator fun invoke(id: Int): PictureOfDayItem? = repository.findById(id)
}