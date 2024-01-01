package com.mariomanzano.kmm_nasa_explorer.ui.viewmodels

import com.mariomanzano.kmm_nasa_explorer.Error
import com.mariomanzano.kmm_nasa_explorer.domain.PictureOfDayItem
import com.mariomanzano.kmm_nasa_explorer.network.toError
import com.mariomanzano.kmm_nasa_explorer.usecases.GetPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODListUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODSingleDayUseCase
import com.mariomanzano.kmm_nasa_explorer.utils.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DailyPictureViewModel(
    getPODUseCase: GetPODUseCase,
    private val requestPODListUseCase: RequestPODListUseCase,
    private val requestPODSingleDayUseCase: RequestPODSingleDayUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { _state.value.copy(loading = true) }
            getPODUseCase()
                .catch { cause ->
                    _state.update { it.copy(error = cause.toError()) }
                }
                .collect { items ->
                    if (items.isNotEmpty()) {
                        _state.update {
                            _state.value.copy(loading = false,
                                dailyPictures = items.sortedByDescending { it.date })
                        }
                    } else {
                        launchListCompleteRequest()
                    }
                    checkIfShowRefresh()
                }
        }
    }

    private suspend fun launchListCompleteRequest() {
        _state.update { _state.value.copy(loading = true, dailyPictures = emptyList()) }
        requestPODListUseCase().fold(ifLeft = { error: Error ->
            _state.value = UiState(
                loading = false,
                dailyPictures = null,
                error = error
            )
        },
            ifRight = { items: List<PictureOfDayItem>? ->
                _state.value = UiState(
                    loading = false,
                    dailyPictures = items,
                    error = null
                )
            })
    }

    fun launchDayRequest() {
        viewModelScope.launch {
            _state.update { _state.value.copy(loading = true) }
            _state.update {
                val error = requestPODSingleDayUseCase().leftOrNull()
                checkIfShowRefresh()
                _state.value.copy(
                    loading = false,
                    error = error
                )
            }
        }
    }

    fun checkIfShowRefresh() {
        _state.update { state ->
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val lastPOD = _state.value.dailyPictures?.first()?.date?.let { LocalDate.parse(it) }
            if (lastPOD != null && !today.sameDay(lastPOD)) {
                state.copy(showRefresh = true)
            } else {
                state.copy(showRefresh = false)
            }
        }
    }


    data class UiState(
        val loading: Boolean = false,
        val dailyPictures: List<PictureOfDayItem>? = null,
        val showRefresh: Boolean = false,
        val error: Error? = null
    )
}

fun LocalDate.sameDay(date: LocalDate): Boolean {
    return dayOfMonth == date.dayOfMonth &&
            month == date.month &&
            year == date.year
}