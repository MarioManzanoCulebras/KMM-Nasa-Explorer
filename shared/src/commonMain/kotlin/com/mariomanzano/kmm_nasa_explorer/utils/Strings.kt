package com.mariomanzano.kmm_nasa_explorer.utils

import androidx.compose.runtime.Composable

expect class Strings {
    fun get(id: String, quantity: Int): String
    fun get(id: String): String
    fun format(id: String, vararg formatArgs: Any): String
}

@Composable
expect fun stringResource(id: String): String

@Composable
expect fun stringResource(id: String, vararg args: Any): String