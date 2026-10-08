package com.trex.liferhythm.ui
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
private val Dark = darkColorScheme(primary = Color(0xFFD4AF5A), background = Color(0xFF0B0F1A), surface = Color(0xFF121826), onSurface = Color(0xFFF3ECDC), onBackground = Color(0xFFF3ECDC), tertiary = Color(0xFF8FA2F0))
private val Light = lightColorScheme(primary = Color(0xFF8F6E1E), background = Color(0xFFF7F2E8), surface = Color(0xFFFFFBF2), onSurface = Color(0xFF1A1F2E), onBackground = Color(0xFF1A1F2E), tertiary = Color(0xFF3F4FA0))
private val Serif = FontFamily.Serif
private val AppTypography = Typography(
    displayMedium = TextStyle(fontFamily = Serif, fontSize = 42.sp, lineHeight = 50.sp),
    headlineLarge = TextStyle(fontFamily = Serif, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontSize = 26.sp),
    titleMedium = TextStyle(fontFamily = Serif, fontSize = 17.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp)
)
@Composable fun LifeRhythmTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, typography = AppTypography, content = content)
}