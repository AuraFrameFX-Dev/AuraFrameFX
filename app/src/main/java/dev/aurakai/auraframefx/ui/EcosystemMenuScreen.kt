package dev.aurakai.auraframefx.ui

import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.R

@Composable
fun EcosystemMenuScreen() {
    val context = LocalContext.current
    val menuImage = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ecosystem_menu)
    }

    // Animation values
    var progress by remember { mutableStateOf(0f) }


    LaunchedEffect(Unit) {
        animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            progress = value
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Draw background glow
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = android.graphics.Color.parseColor("#3300FFCC")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(
                    size.width / 2,
                    size.height / 2,
                    size.width / 2 * progress,
                    paint
                )
            }


            // Draw menu image with animation
            if (menuImage != null) {
                val imageBitmap = menuImage.asImageBitmap()
                val width = size.width
                val height = size.height
                val aspectRatio = menuImage.width.toFloat() / menuImage.height.toFloat()
                val targetWidth = height * aspectRatio

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = android.graphics.Color.parseColor("#00FFCC")
                        style = Paint.Style.STROKE
                        strokeWidth = 4f
                    }


                    // Draw glowing border
                    canvas.drawRect(
                        Offset(0f, 0f),
                        Offset(width, height),
                        paint
                    )


                    // Draw menu image
                    drawImage(
                        imageBitmap,
                        dstSize = IntSize(targetWidth.toInt(), height.toInt()),
                        alpha = progress
                    )
                }
            }
        }
    }
}
