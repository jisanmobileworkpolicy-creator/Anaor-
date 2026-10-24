package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = JisanPrimaryDark,
    onPrimary = Color(0xFF003258),
    primaryContainer = JisanPrimaryDarkContainer,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = JisanSecondaryDark,
    onSecondary = Color(0xFF00363A),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF452B00),
    background = JisanBackgroundDark,
    onBackground = JisanTextPrimaryDark,
    surface = JisanSurfaceDark,
    onSurface = JisanTextPrimaryDark,
    surfaceVariant = JisanSurfaceVariantDark,
    onSurfaceVariant = JisanTextSecondaryDark,
    outline = JisanBorderDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = JisanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = JisanPrimary,
    secondary = JisanSecondary,
    onSecondary = Color.White,
    tertiary = JisanAccent,
    onTertiary = Color.White,
    background = JisanBackground,
    onBackground = JisanTextPrimary,
    surface = JisanSurface,
    onSurface = JisanTextPrimary,
    surfaceVariant = JisanSurfaceVariant,
    onSurfaceVariant = JisanTextSecondary,
    outline = JisanBorder,
    error = JisanRed,
    onError = Color.White
)

@Composable
fun JisanMobileTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
