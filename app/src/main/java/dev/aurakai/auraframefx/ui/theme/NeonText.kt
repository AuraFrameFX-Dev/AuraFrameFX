package dev.aurakai.auraframefx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun NeonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    glowColor: Color = color.copy(alpha = 0.5f),
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    drawStyle: DrawStyle = Fill,
) {
    val glowRadius = 8.dp.value

    Text(
        text = text,
        color = Color.Transparent,
        style = style.copy(
            fontSize = fontSize,
            fontWeight = fontWeight,
            drawStyle = drawStyle,
            shadow = null
        ),
        modifier = modifier.drawBehind {
            // Draw glow
            drawTextWithGlow(
                text = text,
                style = style,
                color = glowColor,
                blurRadius = glowRadius,
                drawStyle = drawStyle
            )
            // Draw main text
            drawText(
                text = text,
                style = style,
                color = color,
                drawStyle = drawStyle
            )
        }
    )
}

private fun DrawScope.drawTextWithGlow(
    text: String,
    style: TextStyle,
    color: Color,
    blurRadius: Float,
    drawStyle: DrawStyle,
) {
    // Draw multiple layers with increasing blur for glow effect
    for (i in 1..5) {
        drawText(
            text = text,
            style = style,
            color = color.copy(alpha = color.alpha * (1f - i * 0.15f)),
            drawStyle = drawStyle,
            alpha = 0.7f - (i * 0.1f)
        )
    }
}

private fun DrawScope.drawText(
    text: String,
    style: TextStyle,
    color: Color,
    drawStyle: DrawStyle,
    alpha: Float = 1f,
) {
    drawContext.canvas.nativeCanvas.apply {
        saveLayer(null, null)
        drawText(
            text = text,
            x = 0f,
            y = 0f,
            paint = style.toSpanStyle().toPaint().asFrameworkPaint().apply {
                this.color = color.copy(alpha = color.alpha * alpha).toArgb()
                this.style = when (drawStyle) {
                    is Fill -> android.graphics.Paint.Style.FILL
                    is Stroke -> android.graphics.Paint.Style.STROKE
                    else -> android.graphics.Paint.Style.FILL
                }
                if (drawStyle is Stroke) {
                    strokeWidth = drawStyle.width
                    strokeJoin = android.graphics.Paint.Join.ROUND
                    strokeCap = android.graphics.Paint.Cap.ROUND
                }
            }
        )
        restore()
    }
}
