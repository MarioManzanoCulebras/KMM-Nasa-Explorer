package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.usecases.FindPODUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DailyPictureDetailViewModel(
    itemId: Int,
    findPODUseCase: FindPODUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        val item = findPODUseCase(itemId)
        _state.update {
            UiState(
                dailyPicture = item,
                error = if (item == null) Error.NoData else null
            )
        }
    }

    data class UiState(
        val dailyPicture: PictureOfDayItem? = null,
        val error: Error? = null
    )
}