package dev.aurakai.auraframefx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.R
import dev.aurakai.auraframefx.service.KaiOverlayService
import dev.aurakai.auraframefx.ui.theme.*

@Composable
fun KaiToolboxScreen(
    hasOverlayPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isOverlayRunning by remember { mutableStateOf(false) }
    
    // Check if overlay service is running
    LaunchedEffect(Unit) {
        isOverlayRunning = KaiOverlayService.isRunning(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.kai_toolbox_title),
                style = MaterialTheme.typography.headlineMedium,
                color = NeonTeal
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.kai_toolbox_back))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Overlay Control Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.overlay_control),
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonTeal
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!hasOverlayPermission) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.grant_overlay_permission))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.overlay_permission_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isOverlayRunning) {
                                    onStopOverlay()
                                } else {
                                    onStartOverlay()
                                }
                                isOverlayRunning = !isOverlayRunning
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOverlayRunning) MaterialTheme.colorScheme.error else NeonTeal
                            )
                        ) {
                            Icon(
                                imageVector = if (isOverlayRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                if (isOverlayRunning) 
                                    stringResource(R.string.stop_overlay) 
                                else 
                                    stringResource(R.string.start_overlay)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Kai's Status Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Kai's Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kai is currently active and monitoring your system.",
                    color = Color.White
                )
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Kai's Controls Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Controls",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Toggle Kai's Toolbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show Kai's Toolbox", color = Color.White)
                    Switch(
                        checked = kaiToolboxVisible,
                        onCheckedChange = { viewModel.toggleKaiToolbox() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonTeal,
                            checkedTrackColor = NeonTeal.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Kai's Stats Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "System Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val systemStats by viewModel.systemStats.collectAsState()
                
                StatItem("CPU Usage", "${systemStats.cpuUsage.toInt()}%")
                StatItem("RAM Usage", "${systemStats.ramUsage.toInt()}%")
                StatItem("Battery", "${systemStats.batteryLevel}%")
                StatItem("Status", if (systemStats.isCharging) "Charging" else "On Battery")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f))
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}
