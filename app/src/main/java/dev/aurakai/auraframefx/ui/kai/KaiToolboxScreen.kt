package dev.aurakai.auraframefx.ui.kai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.aurakai.auraframefx.R
import dev.aurakai.auraframefx.ui.theme.*
import dev.aurakai.auraframefx.viewmodel.KaiViewModel
import kotlin.math.roundToInt

@Composable
fun KaiToolboxScreen(
    viewModel: KaiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var localXOffset by remember(uiState.userXOffsetDp) { 
        mutableFloatStateOf(uiState.userXOffsetDp.value) 
    }
    var localYOffset by remember(uiState.userYOffsetDp) { 
        mutableFloatStateOf(uiState.userYOffsetDp.value) 
    }

    LaunchedEffect(uiState.userXOffsetDp) {
        localXOffset = uiState.userXOffsetDp.value
    }
    LaunchedEffect(uiState.userYOffsetDp) {
        localYOffset = uiState.userYOffsetDp.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(16.dp)
            .verticalScroll(rememberScroll()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kai_toolbox),
                contentDescription = "Kai's Toolbox",
                tint = NeonMagenta,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Kai's Toolbox",
                color = NeonMagenta,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Enable/Disable Orb Feature
        SettingSwitchItem(
            title = "Enable Kai's Notch Orb",
            description = "Show Kai's presence and status near the top of the screen.",
            checked = uiState.isEnabled,
            onCheckedChange = { viewModel.setOrbEnabled(it) },
            highlightColor = NeonMagenta
        )

        Spacer(Modifier.height(24.dp))

        // Orb Position Adjustment
        if (uiState.isEnabled) {
            Text(
                text = "Notch Orb Position",
                color = NeonMagenta,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // X Offset Slider
            Text("Horizontal Offset (X): ${localXOffset.roundToInt()} dp", color = Color.White)
            Slider(
                value = localXOffset,
                onValueChange = { localXOffset = it },
                valueRange = -100f..100f,
                onValueChangeFinished = {
                    viewModel.setOrbOffsets(localXOffset.dp, localYOffset.dp)
                },
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = NeonMagenta,
                    activeTrackColor = NeonMagentaGlow,
                    inactiveTrackColor = NeonMagenta.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Y Offset Slider
            Text("Vertical Offset (Y): ${localYOffset.roundToInt()} dp", color = Color.White)
            Slider(
                value = localYOffset,
                onValueChange = { localYOffset = it },
                valueRange = -50f..150f,
                onValueChangeFinished = {
                    viewModel.setOrbOffsets(localXOffset.dp, localYOffset.dp)
                },
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = NeonMagenta,
                    activeTrackColor = NeonMagentaGlow,
                    inactiveTrackColor = NeonMagenta.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }

        // Placeholder for other Kai Settings
        Divider(color = NeonMagenta.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Advanced Security (Placeholders)",
            color = NeonMagenta,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SettingSwitchItem(
            title = "Enable Ad Blocker", 
            checked = false, 
            onCheckedChange = {},
            highlightColor = NeonMagenta
        )
        SettingSwitchItem(
            title = "Real-time Threat Scan", 
            checked = true, 
            onCheckedChange = {},
            highlightColor = NeonMagenta
        )

        Spacer(Modifier.weight(1f))

        // Back Button
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonMagenta,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 16.dp)
        ) {
            Text("Back to AuraShield")
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                color = highlightColor, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 18.sp
            )
            if (description != null) {
                Text(
                    text = description, 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 14.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = highlightColor,
                checkedTrackColor = highlightColor.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
