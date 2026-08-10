package com.ledgerleaf.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ledgerSerif = FontFamily.Serif

val LedgerLeafTypography = Typography(
    displayLarge = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    headlineLarge = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = ledgerSerif, fontSize = 17.sp),
    bodyMedium = TextStyle(fontFamily = ledgerSerif, fontSize = 15.sp),
    bodySmall = TextStyle(fontFamily = ledgerSerif, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = ledgerSerif, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = ledgerSerif, fontStyle = FontStyle.Italic, fontSize = 11.sp)
)
