package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository

class RequestPODListUseCase(private val repository: DailyPicturesRepository) {

    suspend operator fun invoke() =
        repository.requestPODList()
}