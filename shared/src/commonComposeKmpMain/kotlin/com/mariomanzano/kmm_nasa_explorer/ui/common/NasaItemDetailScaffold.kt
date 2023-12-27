package com.mariomanzano.kmm_nasa_explorer.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mariomanzano.kmm_nasa_explorer.getArrowBack

@Composable
fun NasaItemDetailScaffold(
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            Row {
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = { onBack() }
                ) {
                    Image(
                        painter = getArrowBack(),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        content = content
    )
}
