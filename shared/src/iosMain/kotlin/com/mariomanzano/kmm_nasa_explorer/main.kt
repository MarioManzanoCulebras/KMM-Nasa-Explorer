package com.mariomanzano.kmm_nasa_explorer

import androidx.compose.ui.window.ComposeUIViewController
import com.mariomanzano.kmm_nasa_explorer.ui.NasaExploreApp
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName")
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        initKoin()
        NasaExploreApp()
    }
}