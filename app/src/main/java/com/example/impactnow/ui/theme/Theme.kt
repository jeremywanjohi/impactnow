// File: app/src/main/java/com/example/impactnow/ui/theme/Theme.kt

package com.example.impactnow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightGreen500,
    onPrimary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen500,
    onPrimary = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White
)

@Composable
fun ImpactNowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
