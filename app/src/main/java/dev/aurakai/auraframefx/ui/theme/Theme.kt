package dev.aurakai.auraframefx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAppColor,
    onPrimary = OnPrimaryAppColor,
    primaryContainer = PrimaryContainerColor,
    onPrimaryContainer = PrimaryAppColor,

    secondary = SecondaryAppColor,
    onSecondary = OnSecondaryAppColor,
    secondaryContainer = SecondaryContainerColor,
    onSecondaryContainer = SecondaryAppColor,

    tertiary = TertiaryAppColor,
    onTertiary = OnTertiaryAppColor,
    tertiaryContainer = TertiaryContainerColor,
    onTertiaryContainer = TertiaryAppColor,

    background = BackgroundBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAppColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryAppColor.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryAppColor,

    secondary = SecondaryAppColor,
    onSecondary = Color.White,
    secondaryContainer = SecondaryAppColor.copy(alpha = 0.1f),
    onSecondaryContainer = SecondaryAppColor,

    tertiary = TertiaryAppColor,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryAppColor.copy(alpha = 0.1f),
    onTertiaryContainer = TertiaryAppColor,

    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF666666),
    error = Error,
    onError = Color.White,
)

@Composable
fun AuraFrameFXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Typography is defined in Type.kt
