package com.ledgerleaf.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LedgerDeepGreen,
    onPrimary = LedgerPaper,
    secondary = LedgerBrown,
    tertiary = LedgerAmber,
    error = LedgerRed,
    background = LedgerCream,
    surface = LedgerPaper,
    onBackground = LedgerInk,
    onSurface = LedgerInk
)

private val DarkColors = darkColorScheme(
    primary = DarkGreen,
    secondary = DarkBrown,
    tertiary = DarkAmber,
    error = DarkRed,
    background = DarkForest,
    surface = DarkLedgerPaper,
    onBackground = DarkInk,
    onSurface = DarkInk
)

@Composable
fun LedgerLeafTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LedgerLeafTypography,
        content = content
    )
}
