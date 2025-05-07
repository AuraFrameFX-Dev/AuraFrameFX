package dev.aurakai.auraframefx.ui.theme

import androidx.compose.ui.graphics.Color

// --- Primary Neon Colors ---
val NeonPink = Color(0xFFFF00E1)  // Vibrant pink from Color.kt
val NeonPinkGlow = NeonPink.copy(alpha = 0.5f)

val NeonTeal = Color(0xFF00FFF7)  // Bright teal from Color.kt
val NeonTealGlow = NeonTeal.copy(alpha = 0.5f)

val NeonPurple = Color(0xFFA259FF)  // Rich purple from Color.kt
val NeonPurpleGlow = NeonPurple.copy(alpha = 0.5f)

val NeonBlue = Color(0xFF00FFFF)  // Cyan from Colors.kt
val NeonBlueGlow = NeonBlue.copy(alpha = 0.5f)

val NeonGreen = Color(0xFF00FF00)  // Bright green from Colors.kt
val NeonGreenGlow = NeonGreen.copy(alpha = 0.5f)

// --- Background Colors ---
val BackgroundBlack = Color(0xFF121212)  // Dark background from Colors.kt
val SurfaceDark = Color(0xFF1E1E1E)  // Slightly lighter background
val SurfaceVariantDark = SurfaceDark.copy(alpha = 0.5f)

// --- Text Colors ---
val TextPrimary = Color.White
val TextSecondary = Color(0xB3FFFFFF)  // 70% opacity white
val TextTertiary = Color(0x80FFFFFF)  // 50% opacity white

// --- Status Colors ---
val Success = Color(0xFF00C853)  // Green from Colors.kt
val Warning = Color(0xFFFFD600)   // Yellow from Colors.kt
val Error = Color(0xFFD50000)    // Red from Colors.kt

// --- Utility Colors ---
val Transparent = Color.Transparent
val BlackTransparent = Color(0x80000000)  // 50% black

// --- Material 3 Role Placeholders ---
// These map to the theme's color scheme
val PrimaryAppColor = NeonPink
val OnPrimaryAppColor = Color.Black
val PrimaryContainerColor = NeonPink.copy(alpha = 0.2f)

val SecondaryAppColor = NeonBlue
val OnSecondaryAppColor = Color.Black
val SecondaryContainerColor = NeonBlue.copy(alpha = 0.2f)

val TertiaryAppColor = NeonPurple
val OnTertiaryAppColor = Color.Black
val TertiaryContainerColor = NeonPurple.copy(alpha = 0.2f)

// --- Legacy Colors (marked as deprecated) ---
@Deprecated("Use NeonPurple instead", ReplaceWith("NeonPurple"))
val Purple80 = Color(0xFFD0BCFF)

@Deprecated("Use SurfaceDark instead", ReplaceWith("SurfaceDark"))
val PurpleGrey80 = Color(0xFFCCC2DC)

@Deprecated("Use NeonPink instead", ReplaceWith("NeonPink"))
val Pink80 = Color(0xFFEFB8C8)

@Deprecated("Use NeonPurple instead", ReplaceWith("NeonPurple"))
val Purple40 = Color(0xFF6650A4)

@Deprecated("Use SurfaceVariantDark instead", ReplaceWith("SurfaceVariantDark"))
val PurpleGrey40 = Color(0xFF625B71)

@Deprecated("Use NeonPink instead", ReplaceWith("NeonPink"))
val Pink40 = Color(0xFF7D5260)
