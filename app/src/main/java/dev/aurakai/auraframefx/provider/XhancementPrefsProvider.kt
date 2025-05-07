package dev.aurakai.auraframefx.provider

import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import dev.aurakai.auraframefx.util.PrefsConstants

class XhancementPrefsProvider : RemotePreferenceProvider(
    "${PrefsConstants.AUTHORITY}.provider",
    arrayOf(PrefsConstants.XHANCEMENT_PREFS_NAME)
) {
    override fun onCreate(): Boolean {
        return true
    }
}
