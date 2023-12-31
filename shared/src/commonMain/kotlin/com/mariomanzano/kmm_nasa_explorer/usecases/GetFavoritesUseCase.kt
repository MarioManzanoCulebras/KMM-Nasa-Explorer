package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository

class GetFavoritesUseCase(private val repository: DailyPicturesRepository) {

    operator fun invoke() = repository.podListFavorite
}