package com.mariomanzano.kmm_nasa_explorer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.mariomanzano.kmm_nasa_explorer.ui.screens.DailyPictureScreen
import com.mariomanzano.kmm_nasa_explorer.ui.theme.NasaExplorerTheme

@Composable
fun NasaExploreApp() {
    NasaExploreScreen {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                Navigator(DailyPictureScreen)
            }
        }
    }
}

@Composable
fun NasaExploreScreen(content: @Composable () -> Unit) {
    NasaExplorerTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}