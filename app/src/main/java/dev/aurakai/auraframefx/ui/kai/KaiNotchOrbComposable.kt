package dev.aurakai.auraframefx.ui.kai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aurakai.auraframefx.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Data class for GlowConfig
@Stable
data class KaiGlowConfig(
    val coreColor: Color,
    val glowColor: Color,
    val radius: Dp,
    val animationSpec: AnimationSpec<Float> = tween(500)
)

// Map KaiStatus to visual configurations
val kaiStatusVisuals: Map<KaiStatus, KaiGlowConfig> = mapOf(
    KaiStatus.Disabled to KaiGlowConfig(Color.Transparent, Color.Transparent, 0.dp),
    KaiStatus.Idle to KaiGlowConfig(NeonMagenta.copy(alpha = 0.7f), NeonMagentaGlow.copy(alpha = 0.5f), 20.dp),
    KaiStatus.Monitoring to KaiGlowConfig(NeonMagenta, NeonMagentaGlow, 24.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
    KaiStatus.SystemSecure to KaiGlowConfig(NeonGreen, NeonGreenGlow, 22.dp),
    KaiStatus.ThreatDetected to KaiGlowConfig(NeonErrorRed, NeonErrorRedGlow.copy(alpha = 0.8f), 28.dp, tween(200, easing = FastOutLinearInEasing)),
    KaiStatus.OptimizationActive to KaiGlowConfig(NeonBlue, NeonBlueGlow, 24.dp),
    KaiStatus.Information to KaiGlowConfig(NeonMagenta.copy(alpha = 0.8f), NeonMagentaGlow, 22.dp)
)

@Composable
fun KaiNotchOrbComposable(
    currentStatus: KaiStatus,
    currentText: String? = null,
    userXOffset: Dp = 0.dp,
    userYOffset: Dp = 0.dp,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    if (currentStatus == KaiStatus.Disabled) return@KaiNotchOrbComposable

    val visuals = kaiStatusVisuals[currentStatus] ?: kaiStatusVisuals[KaiStatus.Idle]!!
    val density = LocalDensity.current

    // Animate size (radius)
    val animatedRadiusDp by animateDpAsState(
        targetValue = visuals.radius,
        animationSpec = visuals.animationSpec as DpAnimationSpec<Dp>
            ?: tween(500),
        label = "orbRadius"
    )
    val animatedRadiusPx = with(density) { animatedRadiusDp.toPx() }

    // Animate colors
    val animatedCoreColor by animateColorAsState(visuals.coreColor, label = "orbCoreColor")
    val animatedGlowColor by animateColorAsState(visuals.glowColor, label = "orbGlowColor")

    // Pulsing animation for threat detection
    val threatPulse = remember { Animatable(1f) }
    LaunchedEffect(currentStatus) {
        if (currentStatus == KaiStatus.ThreatDetected) {
            threatPulse.animateTo(
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            threatPulse.snapTo(1f)
        }
    }

    val orbSize = (visuals.radius * 2 * threatPulse.value).coerceAtLeast(10.dp)
    val showText = currentStatus == KaiStatus.Information && !currentText.isNullOrBlank()

    // Calculate the actual width needed if text is shown
    val islandWidth by animateDpAsState(
        targetValue = if (showText) 180.dp else orbSize + 16.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "islandWidth"
    )
    val islandHeight = orbSize + 16.dp

    Box(
        modifier = Modifier
            .offset(x = userXOffset, y = userYOffset)
            .size(width = islandWidth, height = islandHeight)
            .clip(CircleShape)
            .background(animatedCoreColor.copy(alpha = 0.3f))
            .border(1.dp, animatedGlowColor.copy(alpha = 0.7f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Simple Orb Glow
        Canvas(modifier = Modifier.size(orbSize)) {
            // Outer Glow
            drawCircle(
                color = animatedGlowColor,
                radius = animatedRadiusPx * threatPulse.value * 0.8f,
                style = Stroke(width = with(density) { (2.dp * threatPulse.value).toPx() })
            )
            // Inner Core Orb
            drawCircle(
                color = animatedCoreColor,
                radius = animatedRadiusPx * threatPulse.value * 0.6f
            )
        }


        if (showText) {
            Text(
                text = currentText!!,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                modifier = Modifier
                    .padding(start = orbSize + 4.dp)
                    .alpha(if (orbSize < islandWidth - 20.dp) 1f else 0f)
            )
        }
    }
}
