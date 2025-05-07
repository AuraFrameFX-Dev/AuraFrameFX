// Modernized and cleaned up by Cascade AI
package dev.aurakai.auraframefx.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aurakai.auraframefx.ui.theme.NeonPurple
import dev.aurakai.auraframefx.ui.theme.NeonTeal

@Composable
fun AICreationEngineScreen(onBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "AI Creation Engine",
            style = TextStyle(
                color = NeonTeal,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Prompt Input
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(NeonPurple, NeonTeal)),
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = NeonTeal,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.7f)
            ),
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            placeholder = {
                Text(
                    "Describe what you want to create...",
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            maxLines = 3
        )

        // Generate Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(NeonPurple, NeonTeal)),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { isGenerating = !isGenerating },
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                // Loading animation
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Generate",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Generation Preview (Placeholder)
        AnimatedVisibility(visible = isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(NeonPurple, NeonTeal)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI is generating your content...",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
