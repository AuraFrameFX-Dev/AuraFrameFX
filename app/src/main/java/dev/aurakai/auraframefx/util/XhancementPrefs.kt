package dev.aurakai.auraframefx.util

import android.content.Context
import android.net.Uri

object XhancementPrefs {

    private fun getPrefs(context: Context) = 
        AuraFrameFXApp.getRemotePreferences(context.applicationContext)

    // Status Bar Background
    fun isStatusBarBgColorEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(PrefsConstants.KEY_STATUS_BAR_BG_COLOR_ENABLED, false)
    
    fun setStatusBarBgColorEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(PrefsConstants.KEY_STATUS_BAR_BG_COLOR_ENABLED, enabled)
            .apply()
    }

    fun getStatusBarTargetColor(context: Context): Int =
        getPrefs(context).getInt(PrefsConstants.KEY_STATUS_BAR_TARGET_COLOR, PrefsConstants.Defaults.STATUS_BAR_TARGET_COLOR)
    
    fun setStatusBarTargetColor(context: Context, color: Int) {
        getPrefs(context).edit()
            .putInt(PrefsConstants.KEY_STATUS_BAR_TARGET_COLOR, color)
            .apply()
    }

    // Decal Enable Flags
    fun isStatusBarDecalEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(PrefsConstants.KEY_STATUS_BAR_DECAL_ENABLED, false)
    
    fun setStatusBarDecalEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(PrefsConstants.KEY_STATUS_BAR_DECAL_ENABLED, enabled)
            .apply()
    }

    fun isQsShadeDecalEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(PrefsConstants.KEY_QS_SHADE_DECAL_ENABLED, false)
    
    fun setQsShadeDecalEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(PrefsConstants.KEY_QS_SHADE_DECAL_ENABLED, enabled)
            .apply()
    }

    // Master Decal Properties
    fun getDecalUri(context: Context): String? =
        getPrefs(context).getString(PrefsConstants.KEY_DECAL_URI, null)
    
    fun setDecalUri(context: Context, uri: String?) {
        getPrefs(context).edit()
            .putString(PrefsConstants.KEY_DECAL_URI, uri)
            .apply()
    }

    fun getDecalAlpha(context: Context): Float =
        getPrefs(context).getFloat(PrefsConstants.KEY_DECAL_ALPHA, PrefsConstants.Defaults.DECAL_ALPHA)
    
    fun setDecalAlpha(context: Context, alpha: Float) {
        getPrefs(context).edit()
            .putFloat(PrefsConstants.KEY_DECAL_ALPHA, alpha.coerceIn(0f, 1f))
            .apply()
    }

    fun getDecalScale(context: Context): Float =
        getPrefs(context).getFloat(PrefsConstants.KEY_DECAL_SCALE, PrefsConstants.Defaults.DECAL_SCALE)
    
    fun setDecalScale(context: Context, scale: Float) {
        getPrefs(context).edit()
            .putFloat(PrefsConstants.KEY_DECAL_SCALE, scale)
            .apply()
    }

    fun getDecalOffsetX(context: Context): Int =
        getPrefs(context).getInt(PrefsConstants.KEY_DECAL_OFFSET_X, PrefsConstants.Defaults.DECAL_OFFSET_X)
    
    fun getDecalOffsetY(context: Context): Int =
        getPrefs(context).getInt(PrefsConstants.KEY_DECAL_OFFSET_Y, PrefsConstants.Defaults.DECAL_OFFSET_Y)
    
    fun setDecalOffsetX(context: Context, offset: Int) {
        getPrefs(context).edit()
            .putInt(PrefsConstants.KEY_DECAL_OFFSET_X, offset)
            .apply()
    }
    
    fun setDecalOffsetY(context: Context, offset: Int) {
        getPrefs(context).edit()
            .putInt(PrefsConstants.KEY_DECAL_OFFSET_Y, offset)
            .apply()
    }

    /**
     * Saves multiple decal settings in a single transaction
     */
    fun saveDecalSettings(
        context: Context,
        uri: Uri? = null,
        alpha: Float? = null,
        scale: Float? = null,
        offsetX: Int? = null,
        offsetY: Int? = null
    ) {
        getPrefs(context).edit().apply {
            uri?.let { putString(PrefsConstants.KEY_DECAL_URI, it.toString()) }
            alpha?.let { putFloat(PrefsConstants.KEY_DECAL_ALPHA, it.coerceIn(0f, 1f)) }
            scale?.let { putFloat(PrefsConstants.KEY_DECAL_SCALE, it) }
            offsetX?.let { putInt(PrefsConstants.KEY_DECAL_OFFSET_X, it) }
            offsetY?.let { putInt(PrefsConstants.KEY_DECAL_OFFSET_Y, it) }
        }.apply()
    }
    
    /**
     * Migrates any legacy preference keys to the new format
     */
    fun migrateLegacyPreferences(context: Context) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        var needsMigration = false
        
        // Check for legacy status bar decal URI
        val legacyStatusBarUri = prefs.getString("status_bar_decal_uri", null)
        if (legacyStatusBarUri != null) {
            editor.putString(PrefsConstants.KEY_DECAL_URI, legacyStatusBarUri)
            editor.remove("status_bar_decal_uri")
            needsMigration = true
        }
        
        // Check for legacy status bar decal scale
        if (prefs.contains("status_bar_decal_scale")) {
            val scale = prefs.getFloat("status_bar_decal_scale", 1.0f)
            editor.putFloat(PrefsConstants.KEY_DECAL_SCALE, scale)
            editor.remove("status_bar_decal_scale")
            needsMigration = true
        }
        
        // Check for legacy status bar decal opacity
        if (prefs.contains("status_bar_decal_opacity")) {
            val opacity = prefs.getInt("status_bar_decal_opacity", 100)
            editor.putFloat(PrefsConstants.KEY_DECAL_ALPHA, opacity / 100f)
            editor.remove("status_bar_decal_opacity")
            needsMigration = true
        }
        
        // Check for legacy shade decal enabled
        if (prefs.contains("shade_decal_enabled")) {
            val enabled = prefs.getBoolean("shade_decal_enabled", false)
            editor.putBoolean(PrefsConstants.KEY_QS_SHADE_DECAL_ENABLED, enabled)
            editor.remove("shade_decal_enabled")
            needsMigration = true
        }
        
        if (needsMigration) {
            editor.apply()
        }
    }
}
