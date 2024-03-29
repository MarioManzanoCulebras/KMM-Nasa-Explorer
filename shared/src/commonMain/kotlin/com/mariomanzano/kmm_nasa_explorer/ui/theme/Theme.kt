package com.mariomanzano.kmm_nasa_explorer.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = BlueDark,
    primaryVariant = BlueDark,
    secondary = RedDark,
    background = BlackPale
)

private val LightColorPalette = lightColors(
    primary = BlueDark,
    primaryVariant = BlueDark,
    secondary = Red,
    background = BlackPale

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun NasaExplorerTheme(content: @Composable () -> Unit) {

    //Force always to use DarkColorPalette
    /*val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }*/

    MaterialTheme(
        colors = DarkColorPalette,
        // Todo: typography = Typography,
        shapes = Shapes,
        content = content
    )
}