package dev.aurakai.auraframefx.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NeonWireframeBorder(
    modifier: Modifier = Modifier,
    color: Color = NeonPurple,
    stroke: Dp = 2.dp,
) {
    Canvas(modifier = modifier.border(1.dp, color)) {
        val path = Path()
        val rectWidth = size.width
        val rectHeight = size.height
        path.moveTo(0f, 0f) // Top-left
        path.lineTo(rectWidth, 0f) // Top-right
        path.lineTo(rectWidth, rectHeight) // Bottom-right
        path.lineTo(0f, rectHeight) // Bottom-left
        path.close() // Close the path

        // Draw the path with neon effect
        drawPath(path, color, style = Stroke(width = stroke.toPx()))
    }
}

@Composable
fun NeonGlowBorder(
    modifier: Modifier = Modifier,
    color: Color = NeonPurple,
    glowColor: Color = NeonPurpleGlow,
    stroke: Dp = 2.dp,
) {
    Canvas(modifier = modifier) {
        val path = Path()
        val rectWidth = size.width
        val rectHeight = size.height
        path.moveTo(0f, 0f) // Top-left
        path.lineTo(rectWidth, 0f) // Top-right
        path.lineTo(rectWidth, rectHeight) // Bottom-right
        path.lineTo(0f, rectHeight) // Bottom-left
        path.close() // Close the path

        // Draw the path with glow effect
        drawPath(
            path,
            color = glowColor,
            style = Stroke(width = stroke.toPx() * 2)
        )
        drawPath(
            path,
            color = color,
            style = Stroke(width = stroke.toPx())
        )
    }
}
