package dev.aurakai.auraframefx

import android.app.Application
import android.content.Context
import android.util.Log
import com.crossbowffs.remotepreferences.RemotePreferences
import dagger.hilt.android.HiltAndroidApp
import dev.aurakai.auraframefx.util.PrefsConstants
import dev.aurakai.auraframefx.util.XhancementPrefs

@HiltAndroidApp
class AuraFrameFXApp : Application() {

    companion object {
        private const val TAG = "AuraFrameFXApp"
        private const val PREFS_LEGACY_MIGRATED = "prefs_legacy_migrated"

        // Initialize remote preferences for Xposed module
        private var remotePrefs: RemotePreferences? = null

        fun getRemotePreferences(context: Context): RemotePreferences {
            return remotePrefs ?: synchronized(this) {
                RemotePreferences(
                    context.applicationContext,
                    PrefsConstants.AUTHORITY,
                    PrefsConstants.XHANCEMENT_PREFS_NAME,
                    true // Enable file observer
                ).also { remotePrefs = it }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializeModule()
    }

    private fun initializeModule() {
        try {
            // Initialize remote preferences
            remotePrefs = RemotePreferences(
                applicationContext,
                PrefsConstants.AUTHORITY,
                PrefsConstants.XHANCEMENT_PREFS_NAME,
                true // Enable file observer
            )
            
            // Migrate legacy preferences if needed
            migrateLegacyPreferences()
            
            Log.d(TAG, "AuraFrameFX module initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize module", e)
        }
    }
    
    private fun migrateLegacyPreferences() {
        try {
            val prefs = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
            if (!prefs.getBoolean(PREFS_LEGACY_MIGRATED, false)) {
                XhancementPrefs.migrateLegacyPreferences(this)
                prefs.edit().putBoolean(PREFS_LEGACY_MIGRATED, true).apply()
                Log.d(TAG, "Legacy preferences migration completed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during legacy preferences migration", e)
        }
    }
}
