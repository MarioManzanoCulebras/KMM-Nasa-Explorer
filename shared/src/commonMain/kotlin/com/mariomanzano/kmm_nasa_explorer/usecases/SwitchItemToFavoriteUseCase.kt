package com.mariomanzano.kmm_nasa_explorer.usecases

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem

class SwitchItemToFavoriteUseCase(
    private val dailyPicturesRepository: DailyPicturesRepository
) {

    suspend operator fun invoke(item: NasaItem): Error? {
        return dailyPicturesRepository.switchFavorite(item as PictureOfDayItem)
    }
}