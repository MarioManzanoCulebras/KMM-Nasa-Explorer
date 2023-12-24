package com.mariomanzano.kmm_nasa_explorer.utils

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

actual abstract class ViewModel {
    actual val viewModelScope = MainScope()

    protected actual open fun onCleared() {}

    fun clear() {
        onCleared()
        viewModelScope.cancel()
    }
}