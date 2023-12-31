package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import kotlinx.coroutines.flow.Flow

class FindFavoriteUseCase(private val repository: DailyPicturesRepository) {

    operator fun invoke(id: Int, type: String): Flow<NasaItem> =
        repository.findByIdAndType(id, type)
}