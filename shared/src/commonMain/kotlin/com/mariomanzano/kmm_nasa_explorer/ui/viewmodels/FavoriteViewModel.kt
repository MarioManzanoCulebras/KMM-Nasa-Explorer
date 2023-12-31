package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.network.toError
import com.mariomanzano.kmm_nasa_explorer.usecases.GetFavoritesUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private val favoriteList: MutableList<NasaItem> = mutableListOf()

    fun getFavorites() {
        viewModelScope.launch {
            favoriteList.clear()
            _state.update {
                it.copy(loading = true, items = favoriteList)
            }
            getFavoritesUseCase()
                .catch { cause ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = cause.toError()
                        )
                    }
                }
                .collect { items ->
                    favoriteList.addAll(items.sortedBy { it.type })
                    favoriteList
                    if (favoriteList.isEmpty()) {
                        _state.update {
                            it.copy(loading = false, error = Error.NoData)
                        }
                    } else {
                        _state.update {
                            it.copy(
                                loading = false,
                                items = favoriteList.sortedByDescending { favorite -> favorite.date })
                        }
                    }
                }
        }
    }

    data class UiState(
        val loading: Boolean = false,
        val items: List<NasaItem>? = null,
        val error: Error? = null
    )
}