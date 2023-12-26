package com.mariomanzano.kmm_nasa_explorer.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureDetailViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class DailyPictureDetailScreen(val id: Int) : Screen, KoinComponent {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DailyPictureDetailScreen(
            viewModel = rememberScreenModel { DailyPictureDetailViewModel(id, get()) },
            onClose = { navigator.pop() })
    }

}

@Composable
expect fun DailyPictureDetailScreen(viewModel: DailyPictureDetailViewModel, onClose: () -> Unit)