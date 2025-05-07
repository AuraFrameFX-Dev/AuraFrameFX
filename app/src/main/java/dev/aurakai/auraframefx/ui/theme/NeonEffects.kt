package dev.aurakai.auraframefx.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.aurakai.auraframefx.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.rememberCursorPositionProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.BlurEffectFilter
import androidx.compose.ui.graphics.BlurStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable that displays an icon with a customizable glow effect.
 *
 * @param iconResId The resource ID of the icon to display
 * @param contentDescription Description used by accessibility services
 * @param modifier Modifier to be applied to the icon
 * @param tint The tint color of the icon
 * @param glowColor The color of the glow effect (defaults to a brighter version of the tint)
 * @param glowRadius The radius of the glow effect (default: 8.dp)
 * @param glowIntensity The intensity of the glow (0f-1f, default: 0.7f)
 */
@Composable
fun GlowingIcon(
    iconResId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    glowColor: Color = tint.copy(alpha = 0.5f),
    glowRadius: Dp = 8.dp,
    glowIntensity: Float = 0.7f,
) {
    val glowRadiusPx = with(LocalDensity.current) { glowRadius.toPx() }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Glow layer (behind)
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null, // Not read by screen readers
            tint = glowColor,
            modifier = Modifier
                .graphicsLayer {
                    // Slightly larger than the main icon for the glow effect
                    scaleX = 1.1f
                    scaleY = 1.1f
                }
                .blur(glowRadiusPx)
                .alpha(glowIntensity)
        )
        
        // Main icon (on top)
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * A more advanced glow effect using custom drawing for better performance
 * and more control over the glow appearance.
 */
@Composable
fun AdvancedGlowingIcon(
    iconResId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    glowColor: Color = tint.copy(alpha = 0.5f),
    glowRadius: Dp = 12.dp,
    glowIntensity: Float = 0.8f,
) {
    val glowRadiusPx = with(LocalDensity.current) { glowRadius.toPx() }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .drawWithCache {
                val paint = Paint().asFrameworkPaint()
                val glowPaint = Paint().asFrameworkPaint().apply {
                    color = glowColor.copy(alpha = glowIntensity).toArgb()
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(
                        glowRadiusPx,
                        BlurMaskFilter.Blur.NORMAL
                    )
                }
                
                onDrawWithContent {
                    // Draw the glow
                    drawIntoCanvas { canvas ->
                        // Draw multiple layers for a more intense glow
                        repeat(3) {
                            canvas.nativeCanvas.drawCircle(
                                size.width / 2,
                                size.height / 2,
                                (size.minDimension / 2) * 0.8f,
                                glowPaint
                            )
                        }
                    }
                    // Draw the actual icon
                    drawContent()
                }
            }
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GlowingIconPreview() {
    MaterialTheme {
        Surface(
            color = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Simple glow effect
                GlowingIcon(
                    iconResId = R.drawable.ic_launcher_foreground, // Replace with your actual icon
                    contentDescription = "Glowing Toolbox Icon",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFF00FF), // Neon magenta
                    glowColor = Color(0xFFFF00FF).copy(alpha = 0.7f),
                    glowRadius = 12.dp
                )
                
                // Advanced glow effect
                AdvancedGlowingIcon(
                    iconResId = R.drawable.ic_launcher_foreground, // Replace with your actual icon
                    contentDescription = "Advanced Glowing Toolbox Icon",
                    modifier = Modifier.size(96.dp),
                    tint = Color(0xFF00FFFF), // Neon cyan
                    glowColor = Color(0xFF00FFFF).copy(alpha = 0.6f),
                    glowRadius = 16.dp,
                    glowIntensity = 1f
                )
            }
        }
    }
}

/**
 * A popup that shows a glowing icon at a specific screen position.
 * Useful for floating UI elements that need to follow touch/mouse position.
 */
@Composable
fun FloatingGlowingIcon(
    iconResId: Int,
    contentDescription: String?,
    position: IntOffset,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(48.dp, 48.dp),
    tint: Color = Color.White,
    glowColor: Color = Color.Cyan,
    onDismissRequest: () -> Unit = {}
) {
    val density = LocalDensity.current
    
    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            excludeFromSystemGesture = true
        )
    ) {
        Box(
            modifier = modifier
                .offset { position }
                .size(size)
        ) {
            AdvancedGlowingIcon(
                iconResId = iconResId,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                tint = tint,
                glowColor = glowColor,
                glowRadius = 16.dp,
                glowIntensity = 0.9f
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloatingGlowingIconPreview() {
    MaterialTheme {
        Surface(
            color = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxSize()
        ) {
            var position by remember { mutableStateOf(IntOffset(100, 100)) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            position = IntOffset(offset.x, offset.y)
                        }
                    }
            ) {
                FloatingGlowingIcon(
                    iconResId = R.drawable.ic_launcher_foreground, // Replace with your actual icon
                    contentDescription = "Floating Glowing Icon",
                    position = position,
                    size = DpSize(64.dp, 64.dp),
                    tint = Color(0xFFFF00FF),
                    glowColor = Color(0xFFFF00FF).copy(alpha = 0.7f)
                )
                
                // Instructions
                Text(
                    text = "Tap anywhere to move the glowing icon",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * A composable that displays an icon with a customizable glow effect.
 *
 * @param iconResId The resource ID of the icon to display
 * @param contentDescription Description used by accessibility services
 * @param modifier Modifier to be applied to the icon
 * @param tint The tint color of the icon
 * @param glowColor The color of the glow effect (defaults to a brighter version of the tint)
 * @param glowRadius The radius of the glow effect (default: 8.dp)
 * @param glowIntensity The intensity of the glow (0f-1f, default: 0.7f)
 */
@Composable
fun GlowingIcon(
    iconResId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    glowColor: Color = tint.copy(alpha = 0.5f),
    glowRadius: Dp = 8.dp,
    glowIntensity: Float = 0.7f,
) {
    val glowRadiusPx = glowRadius.toPx()
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Glow layer (behind)
        androidx.compose.material3.Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null, // Not read by screen readers
            tint = glowColor,
            modifier = Modifier
                .graphicsLayer {
                    // Slightly larger than the main icon for the glow effect
                    scaleX = 1.1f
                    scaleY = 1.1f
                }
                .blur(glowRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .alpha(glowIntensity)
        )
        
        // Main icon (on top)
        androidx.compose.material3.Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * A more advanced glow effect using custom drawing for better performance
 * and more control over the glow appearance.
 */
@Composable
fun AdvancedGlowingIcon(
    iconResId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    glowColor: Color = tint.copy(alpha = 0.5f),
    glowRadius: Dp = 12.dp,
    glowIntensity: Float = 0.8f,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .drawWithCache {
                val paint = Paint().asFrameworkPaint()
                val glowPaint = Paint().asFrameworkPaint().apply {
                    color = glowColor.copy(alpha = glowIntensity).toArgb()
                    isAntiAlias = true
                    maskFilter = android.graphics.BlurMaskFilter(
                        glowRadius.toPx(),
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                }
                
                onDrawWithContent {
                    // Draw the glow
                    drawIntoCanvas { canvas ->
                        // Draw multiple layers for a more intense glow
                        repeat(3) {
                            canvas.nativeCanvas.drawCircle(
                                size.width / 2,
                                size.height / 2,
                                (size.minDimension / 2) * 0.8f,
                                glowPaint
                            )
                        }
                    }
                    // Draw the actual icon
                    drawContent()
                }
            }
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.matchParentSize()
        )
    }
}
