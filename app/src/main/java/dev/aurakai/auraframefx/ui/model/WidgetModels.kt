package dev.aurakai.auraframefx.ui.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.ui.theme.*

// Widget Configuration
data class WidgetConfig(
    val text: String = "",
    val color: Color = NeonTeal,
    val size: WidgetSize = WidgetSize.MEDIUM,
    val isGlowing: Boolean = true,
    val iconResId: Int? = null
)

// Widget Size Options
enum class WidgetSize(val value: Dp) {
    SMALL(60.dp),
    MEDIUM(90.dp),
    LARGE(120.dp)
}

// Widget State
data class WidgetState(
    val id: Int,
    var type: WidgetType,
    var offset: Offset,
    var isVisible: Boolean = true,
    var config: WidgetConfig = WidgetConfig()
)

// Widget Types
enum class WidgetType {
    CLOCK,
    BATTERY,
    NEON_HEART,
    CUSTOM_TEXT,
    MUSIC_PLAYER,
    CPU_USAGE,
    RAM_USAGE,
    NETWORK_STATS,
    STATIC_IMAGE,
    CREATIVE_SPARK_TIP
}

// Glow Effect Types
enum class GlowType(val color: Color) {
    SIDEBAR_TOGGLE(NeonTeal),
    BOOST_COMPLETE(NeonGreen),
    SECURITY_ALERT(NeonErrorRed),
    CREATIVE_SPARK(NeonPink),
    SYSTEM_ALERT(NeonBlue)
}

// System Stats Data Class
data class SystemStats(
    val cpuUsage: Float = 0f,
    val ramUsage: Float = 0f,
    val networkUp: Long = 0,
    val networkDown: Long = 0,
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false
)
