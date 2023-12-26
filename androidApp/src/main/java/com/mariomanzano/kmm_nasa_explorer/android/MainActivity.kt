package com.mariomanzano.kmm_nasa_explorer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mariomanzano.kmm_nasa_explorer.ui.NasaExploreApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NasaExploreApp()
        }
    }
}
