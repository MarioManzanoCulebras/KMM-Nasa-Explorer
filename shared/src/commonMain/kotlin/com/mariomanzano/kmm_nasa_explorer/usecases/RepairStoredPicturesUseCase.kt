package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository

/** Fixes the pictures stored by versions of the app that used the legacy NASA API. */
class RepairStoredPicturesUseCase(private val repository: DailyPicturesRepository) {

    suspend operator fun invoke() = repository.repairStoredPictures()
}
