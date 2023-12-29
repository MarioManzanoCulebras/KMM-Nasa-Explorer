package com.mariomanzano.kmm_nasa_explorer.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class Strings(
    private val context: Context
) {
    actual fun get(id: String): String {
        val resourceId = context.resources.getIdentifier(id, "string", context.packageName)
        if (resourceId == 0) return id
        return context.getString(resourceId)
    }

    actual fun get(id: String, quantity: Int): String {
        val resourceId = context.resources.getIdentifier(id, "plurals", context.packageName)
        if (resourceId == 0) return id
        return context.resources.getQuantityString(resourceId, quantity, quantity)
    }

    actual fun format(id: String, vararg formatArgs: Any): String {
        val resourceId = context.resources.getIdentifier(id, "string", context.packageName)
        if (resourceId == 0) return id
        return context.resources.getString(resourceId, *formatArgs)
    }
}

@Composable
actual fun stringResource(id: String): String {
    return Strings(LocalContext.current).get(id)
}

@Composable
actual fun stringResource(id: String, vararg args: Any): String {
    return Strings(LocalContext.current).format(id, args)
}