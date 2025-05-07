package dev.aurakai.auraframefx.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun IntroScreen(onIntroComplete: () -> Unit) {
    // Animation values
    var progress by remember { mutableStateOf(0f) }
    val transition = rememberInfiniteTransition()
    val glowRadius by transition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Text properties
    val text = "AURAFRAMEFX"
    val density = LocalDensity.current
    val textSize = with(density) { 80.dp.toPx() }
    val spacing = with(density) { 20.dp.toPx() }

    // Start animation
    LaunchedEffect(Unit) {
        try {
            animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(3000, easing = FastOutSlowInEasing)
            ) { value, _ ->
                progress = value
                if (value >= 1f) {
                    onIntroComplete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onIntroComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Draw background glow
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = Color(0x3300FFCC) // Semi-transparent teal
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(
                    size.width / 2,
                    size.height / 2,
                    glowRadius,
                    paint
                )
            }


            // Draw each letter with animation
            val paint = Paint().apply {
                color = Color(0xFF00FFCC) // Neon teal
                textSize = textSize
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            Path()
            val textWidth = paint.measureText(text)
            val x = (size.width - textWidth) / 2
            val y = size.height / 2

            // Draw each letter with animation
            for ((index, char) in text.toCharArray().withIndex()) {
                val letterProgress = progress * (index + 1) / text.length
                if (letterProgress > 0) {
                    // Draw letter
                    drawIntoCanvas { canvas ->
                        paint.color = Color(0xFF00FFCC) // Neon teal
                        canvas.drawText(
                            char.toString(),
                            x + (index * (paint.measureText(char.toString()) + spacing)),
                            y.toFloat(),
                            paint
                        )
                    }
                }
            }
        }
    }
}
