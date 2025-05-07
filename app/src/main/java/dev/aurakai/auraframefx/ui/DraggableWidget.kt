package dev.aurakai.auraframefx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.ui.model.WidgetConfig
import dev.aurakai.auraframefx.ui.model.WidgetType
import kotlin.math.roundToInt

@Composable
fun DraggableWidget(
    widgetType: WidgetType,
    initialOffset: Offset,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    config: WidgetConfig = WidgetConfig()
) {
    var offset by remember { mutableStateOf(initialOffset) }

    // Get display text and color from config
    val textColor = config.color
    val displayText = when (widgetType) {
        WidgetType.CLOCK -> "🕒 ${config.text.ifEmpty { "10:10" }}"
        WidgetType.BATTERY -> "🔋 ${config.text.ifEmpty { "90%" }}"
        WidgetType.CUSTOM_TEXT -> config.text.ifEmpty { "Custom Text" }
        WidgetType.MUSIC_PLAYER -> "🎵 ${config.text.ifEmpty { "Music" }}"
        WidgetType.CPU_USAGE -> "CPU: ${config.text.ifEmpty { "45%" }}"
        WidgetType.RAM_USAGE -> "RAM: ${config.text.ifEmpty { "60%" }}"
        WidgetType.NETWORK_STATS -> "Net: ${config.text.ifEmpty { "1Mb/s" }}"
        WidgetType.NEON_HEART -> "" // Special case, might draw NeonHeart composable
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        offset += dragAmount
                    },
                    onDragEnd = {
                        onDragEnd(offset)
                    }
                )
            }
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        if (widgetType == WidgetType.NEON_HEART) {
            // TODO: Add NeonHeart composable if needed
            Text("❤️", color = textColor)
        } else {
            Text(displayText, color = textColor)
        }
    }
}
