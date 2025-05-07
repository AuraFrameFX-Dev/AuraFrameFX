package dev.aurakai.auraframefx.util

/**
 * Constants for shared preferences used across the app and Xposed module.
 * These keys must match between the app UI (XhancementPrefs) and the Xposed module (AuraXposedEntry).
 */
object PrefsConstants {
    // Authority for the RemotePreferenceProvider
    // This must match the authority in AndroidManifest.xml for XhancementPrefsProvider
    const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

    // Name of the shared preferences file used by RemotePreferences
    const val XHANCEMENT_PREFS_NAME = "xhancement_prefs"

    // --- Status Bar Background Color ---
    const val KEY_STATUS_BAR_BG_COLOR_ENABLED = "status_bar_bg_color_enabled"
    const val KEY_STATUS_BAR_TARGET_COLOR = "status_bar_target_color" // Int (Color)

    // --- Decal Settings (Master Settings, can be used by SB and QS) ---
    const val KEY_STATUS_BAR_DECAL_ENABLED = "status_bar_decal_enabled" // Boolean
    const val KEY_QS_SHADE_DECAL_ENABLED = "qs_shade_decal_enabled"     // Boolean

    // Master decal settings - used when either status bar or QS decal is enabled
    const val KEY_DECAL_URI = "master_decal_uri"           // String (Content URI)
    const val KEY_DECAL_ALPHA = "master_decal_alpha"       // Float (0.0f to 1.0f)
    const val KEY_DECAL_SCALE = "master_decal_scale"       // Float
    const val KEY_DECAL_OFFSET_X = "master_decal_offset_x" // Int (pixels or dp - be consistent)
    const val KEY_DECAL_OFFSET_Y = "master_decal_offset_y" // Int

    // Legacy keys for backward compatibility with existing preferences
    @Deprecated("Use KEY_STATUS_BAR_DECAL_ENABLED instead", ReplaceWith("KEY_STATUS_BAR_DECAL_ENABLED"))
    const val KEY_SHADE_DECAL_ENABLED = "shade_decal_enabled"

    @Deprecated("Use KEY_DECAL_URI instead", ReplaceWith("KEY_DECAL_URI"))
    const val KEY_STATUS_BAR_DECAL_URI = "status_bar_decal_uri"

    @Deprecated("Use KEY_DECAL_ALPHA instead (convert from 0-100 to 0.0-1.0)", ReplaceWith("KEY_DECAL_ALPHA"))
    const val KEY_STATUS_BAR_DECAL_OPACITY = "status_bar_decal_opacity" // Int 0-100

    @Deprecated("Use KEY_DECAL_ALPHA instead (convert from 0-100 to 0.0-1.0)", ReplaceWith("KEY_DECAL_ALPHA"))
    const val KEY_SHADE_DECAL_OPACITY = "shade_decal_opacity" // Int 0-100

    @Deprecated("Use KEY_DECAL_SCALE instead", ReplaceWith("KEY_DECAL_SCALE"))
    const val KEY_STATUS_BAR_DECAL_SCALE = "status_bar_decal_scale"

    @Deprecated("Use KEY_DECAL_SCALE instead", ReplaceWith("KEY_DECAL_SCALE"))
    const val KEY_SHADE_DECAL_SCALE = "shade_decal_scale"

    // Default values
    object Defaults {
        const val DECAL_ALPHA = 1.0f
        const val DECAL_SCALE = 1.0f
        const val DECAL_OFFSET_X = 0
        const val DECAL_OFFSET_Y = 0
        const val STATUS_BAR_TARGET_COLOR = 0x00000000 // Transparent black
    }

    /**
     * Converts the old opacity Int (0-100) to alpha Float (0.0-1.0)
     */
    fun convertOpacityToAlpha(opacity: Int): Float {
        return opacity.coerceIn(0, 100) / 100f
    }

    /**
     * Converts alpha Float (0.0-1.0) to the old opacity Int (0-100)
     */
    fun convertAlphaToOpacity(alpha: Float): Int {
        return (alpha.coerceIn(0f, 1f) * 100).toInt()
    }
}
