package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.network.toError
import com.mariomanzano.kmm_nasa_explorer.usecases.FindFavoriteUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.SwitchItemToFavoriteUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteDetailViewModel(
    itemId: Int,
    type: String,
    private val findFavoriteUseCase: FindFavoriteUseCase,
    private val switchFavoriteUseCase: SwitchItemToFavoriteUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            findFavoriteUseCase(itemId, type)
                .catch { cause -> _state.update { it.copy(error = cause.toError()) } }
                .collect { item -> _state.update { UiState(nasaItem = item) } }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            _state.value.nasaItem?.let { item ->
                val error = switchFavoriteUseCase(item)
                _state.update { it.copy(error = error) }
            }
        }
    }

    data class UiState(
        val nasaItem: NasaItem? = null,
        val error: Error? = null
    )
}