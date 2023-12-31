package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import cafe.adriel.voyager.core.model.ScreenModel
import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.toError
import com.mariomanzano.kmm_nasa_explorer.usecases.FindPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.SwitchItemToFavoriteUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyPictureDetailViewModel(
    itemId: Int,
    findPODUseCase: FindPODUseCase,
    private val favoriteUseCase: SwitchItemToFavoriteUseCase
) : ViewModel(), ScreenModel {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            findPODUseCase(itemId)
                .catch { cause -> _state.update { it.copy(error = cause.toError()) } }
                .collect { dailyPicture -> _state.update { UiState(dailyPicture = dailyPicture) } }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            _state.value.dailyPicture?.let { item ->
                val error = favoriteUseCase(item)
                _state.update { it.copy(error = error) }
            }
        }
    }

    data class UiState(
        val dailyPicture: PictureOfDayItem? = null,
        val error: Error? = null
    )
}