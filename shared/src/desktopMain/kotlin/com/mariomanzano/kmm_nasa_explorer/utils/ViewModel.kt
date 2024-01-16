package com.mariomanzano.kmm_nasa_explorer.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

actual abstract class ViewModel {
    actual val viewModelScope: CoroutineScope = MainScope()

    protected actual open fun onCleared() {}
}