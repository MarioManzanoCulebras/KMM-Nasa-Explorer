package com.mariomanzano.kmm_nasa_explorer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mariomanzano.kmm_nasa_explorer.di.getBaseModules
import com.mariomanzano.kmm_nasa_explorer.ui.NasaExploreApp
import org.koin.core.context.startKoin

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication, title = getPlatform().name) {
            startKoin {
                modules(getBaseModules())
            }
            NasaExploreApp()
        }
    }
}
