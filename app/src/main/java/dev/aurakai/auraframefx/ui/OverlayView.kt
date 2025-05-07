package dev.aurakai.auraframefx.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.aurakai.auraframefx.ui.model.WidgetConfig
import dev.aurakai.auraframefx.ui.model.WidgetState
import dev.aurakai.auraframefx.ui.model.WidgetType
import dev.aurakai.auraframefx.ui.theme.NeonPink
import dev.aurakai.auraframefx.ui.theme.NeonText
import kotlinx.coroutines.delay

@Composable
fun OverlayView(
    onStopService: () -> Unit,
) {
    // Aura's Creative Spark State
    val creativeTips = remember {
        listOf(
            "Try combining neon shapes!",
            "Adjust overlay transparency?",
            "Drag widgets for a unique layout!",
            "What if the clock was magenta?",
            "Experiment with background images!",
            "A little glow goes a long way.",
            "Organize widgets logically.",
            "Consider accessibility in your design."
        )
    }
    var currentTip by remember { mutableStateOf<String?>(null) }
    var tipVisible by remember { mutableStateOf(false) }

    // Effect to periodically show a tip
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L) // 30 seconds between tips
            if (!tipVisible) {
                currentTip = creativeTips.random()
                tipVisible = true
                delay(8000L) // Show tip for 8 seconds
                tipVisible = false
                currentTip = null
            }
        }
    }
    // State for widgets
    val (widgets, setWidgets) = remember {
        mutableStateOf(
            listOf(
                WidgetState(
                    id = 1,
                    type = WidgetType.CLOCK,
                    offset = Offset(100f, 100f),
                    config = WidgetConfig()
                ),
                WidgetState(
                    id = 2,
                    type = WidgetType.BATTERY,
                    offset = Offset(500f, 1000f),
                    config = WidgetConfig()
                )
            )
        )
    }

    var nextId by remember { mutableStateOf(3) }
    var showGridMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Render all widgets
        widgets.forEach { widget ->
            if (widget.isVisible) {
                DraggableWidget(
                    widgetType = widget.type,
                    initialOffset = widget.offset,
                    onDragEnd = { newOffset ->
                        setWidgets(widgets.map { w ->
                            if (w.id == widget.id) w.copy(offset = newOffset) else w
                        })
                    },
                    config = widget.config
                )
            }
        }


        // Add Widget Button
        Button(
            onClick = { showGridMenu = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("Add Widget")
        }

        // Widget Grid Menu
        if (showGridMenu) {
            // Simple widget selection menu
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Add Widget", color = Color.White, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Widget Buttons
                    val widgetTypes = listOf(
                        WidgetType.CLOCK to "🕒 Clock",
                        WidgetType.BATTERY to "🔋 Battery",
                        WidgetType.CPU_USAGE to "⚡ CPU",
                        WidgetType.RAM_USAGE to "💾 RAM",
                        WidgetType.NETWORK_STATS to "🌐 Network",
                        WidgetType.MUSIC_PLAYER to "🎵 Music",
                        WidgetType.NEON_HEART to "❤️ Heart"
                    )

                    widgetTypes.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { (type, label) ->
                                Button(
                                    onClick = {
                                        setWidgets(
                                            widgets + WidgetState(
                                                id = (widgets.maxOfOrNull { it.id } ?: 0) + 1,
                                                type = type,
                                                offset = Offset(100f, 100f),
                                                config = WidgetConfig()
                                            )
                                        )
                                        showGridMenu = false
                                    }
                                ) {
                                    Text(label)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showGridMenu = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Close")
                    }
                }
            }
        }

        // Creative Tips
        AnimatedVisibility(
            visible = tipVisible && currentTip != null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp),
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(1.dp, NeonPink, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                NeonText(
                    text = "💡 Aura: ${currentTip ?: ""}",
                    color = NeonPink,
                    glowColor = NeonPink.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }

        // Close Button
        Button(
            onClick = onStopService,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Close")
        }

        // Widget Selection Menu
        if (showGridMenu) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.DarkGray.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text("Add Widget", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))

                // Widget Type Buttons
                WidgetType.values().forEach { type ->
                    Button(
                        onClick = {
                            setWidgets(
                                widgets + WidgetState(
                                    id = nextId++,
                                    type = type,
                                    offset = Offset(200f, 400f)
                                )
                            )
                            showGridMenu = false
                        }
                    ) {
                        Text(type.name)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Close Menu Button
                Button(
                    onClick = { showGridMenu = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
