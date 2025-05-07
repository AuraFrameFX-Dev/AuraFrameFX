package dev.aurakai.auraframefx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aurakai.auraframefx.ui.theme.NeonPurple
import dev.aurakai.auraframefx.ui.theme.NeonTeal
import dev.aurakai.auraframefx.ui.theme.NeonMagenta

@Composable
fun MenuScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToEcosystem: () -> Unit,
    onNavigateToKaiToolbox: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "AuraFrameFX",
            color = NeonTeal,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onNavigateToChat,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Chat / Texting", color = Color.White)
        }
        Button(
            onClick = onNavigateToEcosystem,
            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Ecosystem", color = Color.Black)
        }
        Button(
            onClick = onNavigateToKaiToolbox,
            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Kai's Toolbox", color = Color.White)
        }
    }
}
