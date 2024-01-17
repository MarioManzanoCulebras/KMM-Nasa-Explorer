package com.mariomanzano.kmm_nasa_explorer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mariomanzano.kmm_nasa_explorer.di.getBaseModules
import com.mariomanzano.kmm_nasa_explorer.ui.NasaExploreApp
import org.koin.core.context.startKoin

fun main() {
    application {
        val windowState = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = getPlatform().name
        ) {
            startKoin {
                modules(getBaseModules())
            }
            NasaExploreApp()
        }
    }
}
