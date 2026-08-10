package com.ledgerleaf.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LedgerDeepGreen,
    onPrimary = LedgerPaper,
    primaryContainer = Color(0xFFDDE9D3),
    onPrimaryContainer = LedgerInk,
    secondary = LedgerBrown,
    onSecondary = LedgerPaper,
    secondaryContainer = Color(0xFFEFE7D3),
    onSecondaryContainer = LedgerInk,
    tertiary = LedgerAmber,
    onTertiary = LedgerPaper,
    tertiaryContainer = Color(0xFFF3E4C7),
    onTertiaryContainer = LedgerInk,
    error = LedgerRed,
    background = LedgerCream,
    onBackground = LedgerInk,
    surface = LedgerPaper,
    onSurface = LedgerInk,
    surfaceVariant = LedgerPaperAlt,
    onSurfaceVariant = LedgerInkSoft,
    outline = LedgerInkFaint,
    outlineVariant = LedgerHairline
)

private val DarkColors = darkColorScheme(
    primary = DarkGreen,
    onPrimary = Color(0xFF0B1208),
    primaryContainer = Color(0xFF35431F),
    onPrimaryContainer = DarkInk,
    secondary = DarkBrown,
    onSecondary = DarkForest,
    secondaryContainer = Color(0xFF1C1F16),
    onSecondaryContainer = DarkInkSoft,
    tertiary = DarkAmber,
    onTertiary = DarkForest,
    tertiaryContainer = Color(0xFF34291B),
    onTertiaryContainer = DarkInk,
    error = DarkRed,
    background = DarkForest,
    onBackground = DarkInk,
    surface = DarkCard,
    onSurface = DarkInk,
    surfaceVariant = DarkLedgerPaperAlt,
    onSurfaceVariant = DarkInkSoft,
    outline = DarkInkFaint,
    outlineVariant = DarkHairline
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

