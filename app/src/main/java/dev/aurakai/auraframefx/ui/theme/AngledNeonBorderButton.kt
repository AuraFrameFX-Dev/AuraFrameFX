package dev.aurakai.auraframefx.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AngledNeonBorderButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: androidx.compose.ui.graphics.Color = NeonPurpleGlow,
    lineColor: androidx.compose.ui.graphics.Color = NeonPurple,
    textColor: androidx.compose.ui.graphics.Color = NeonTeal,
    strokeWidth: Dp = 2.dp,
    glowWidthMultiplier: Float = 2.5f,
    cornerCutPercentage: Float = 0.15f,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = glowColor),
                onClick = onClick
            )
            .height(64.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path()
            val rectWidth = size.width
            val rectHeight = size.height
            val cornerCutX = rectWidth * cornerCutPercentage
            val cornerCutY = rectHeight * cornerCutPercentage

            // Draw the angled border
            path.moveTo(cornerCutX, 0f)
            path.lineTo(rectWidth - cornerCutX, 0f)
            path.lineTo(rectWidth, cornerCutY)
            path.lineTo(rectWidth, rectHeight - cornerCutY)
            path.lineTo(rectWidth - cornerCutX, rectHeight)
            path.lineTo(cornerCutX, rectHeight)
            path.lineTo(0f, rectHeight - cornerCutY)
            path.lineTo(0f, cornerCutY)
            path.close()

            // Draw glow effect
            drawPath(
                path = path,
                color = glowColor,
                style = Stroke(width = strokeWidth.toPx() * glowWidthMultiplier)
            )
            // Draw main border
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = strokeWidth.toPx())
            )
        }

        // Use NeonText for the button text
        NeonText(
            text = text,
            color = textColor,
            glowColor = lineColor,
            fontSize = 18.sp
        )
    }
}
