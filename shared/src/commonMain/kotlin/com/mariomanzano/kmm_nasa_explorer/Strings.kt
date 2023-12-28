package com.mariomanzano.kmm_nasa_explorer

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource

expect class Strings {
    fun get(id: StringResource, args: List<Any>): String
}

@Composable
expect fun stringResource(id: StringResource, vararg args: Any): String