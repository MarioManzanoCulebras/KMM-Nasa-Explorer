package com.mariomanzano.kmm_nasa_explorer.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.useResource

actual class Strings {
    actual fun get(id: String): String {
        return ""
    }

    actual fun get(id: String, quantity: Int): String {
        return ""
    }

    actual fun format(id: String, vararg formatArgs: Any): String {
        return ""
    }
}

@Composable
actual fun stringResource(id: String): String {
    val stringFile = useResource("strings.xml") {
        it.bufferedReader().readText()
    }
    val startIdIndex = stringFile.indexOf(id)
    val valueStartIndex = startIdIndex + id.length + 2
    val valueLine = stringFile.substring(valueStartIndex)
    val valueEndIndex = valueLine.indexOf('<')
    return valueLine.substring(0, valueEndIndex)
}

@Composable
actual fun stringResource(id: String, vararg args: Any): String {
    return ""
}