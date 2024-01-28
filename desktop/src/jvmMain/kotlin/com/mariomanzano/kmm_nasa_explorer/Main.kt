package com.mariomanzano.kmm_nasa_explorer

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mariomanzano.kmm_nasa_explorer.di.getBaseModules
import com.mariomanzano.kmm_nasa_explorer.ui.NasaExploreApp
import org.koin.core.context.startKoin

fun main() {
    application {
        val windowState = rememberWindowState(size = DpSize(300.dp, 1000.dp))
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Nasa Explorer KMM"
        ) {
            startKoin {
                modules(getBaseModules())
            }
            NasaExploreApp()
        }
    }
}
