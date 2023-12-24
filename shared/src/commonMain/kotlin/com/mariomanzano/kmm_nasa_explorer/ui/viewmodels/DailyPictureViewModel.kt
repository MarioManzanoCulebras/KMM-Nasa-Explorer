package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.usecases.GetPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODListUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyPictureViewModel(
    getPODUseCase: GetPODUseCase,
    private val requestPODListUseCase: RequestPODListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        val list = getPODUseCase()
        if (!list.isNullOrEmpty()) {
            _state.update {
                _state.value.copy(loading = false,
                    error = null,
                    dailyPictures = list.sortedByDescending { it.date })
            }
        } else {
            launchListCompleteRequest()
        }
    }

    private fun launchListCompleteRequest() {
        viewModelScope.launch {
            _state.update { _state.value.copy(loading = true, dailyPictures = emptyList()) }
            _state.value = UiState(
                loading = false,
                dailyPictures = requestPODListUseCase().getOrNull(),
                error = requestPODListUseCase().leftOrNull()
            )
        }
    }

    data class UiState(
        val loading: Boolean = false,
        val dailyPictures: List<PictureOfDayItem>? = null,
        val error: Error? = null
    )
}