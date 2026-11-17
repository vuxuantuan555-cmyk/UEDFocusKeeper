package com.ued.focuskeeper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BluePrimary = Color(0xFF4F8EF7)
val BlueGlow = Color(0x804F8EF7)
val GreenAccent = Color(0xFF22D3A5)
val RedAccent = Color(0xFFF75F5F)
val YellowAccent = Color(0xFFF7C948)
val BgDark = Color(0xFF0D0F1A)
val SurfaceDark = Color(0xFF141728)
val CardDark = Color(0xFF1A1F35)
val BorderColor = Color(0xFF252A45)
val TextPrimary = Color(0xFFE8EAF6)
val TextMuted = Color(0xFF6B7280)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = GreenAccent,
    tertiary = YellowAccent,
    error = RedAccent,
    background = BgDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderColor,
    surfaceVariant = CardDark,
)

@Composable
fun UEDFocusKeeperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
