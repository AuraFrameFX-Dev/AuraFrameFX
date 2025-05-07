package dev.aurakai.auraframefx.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aurakai.auraframefx.ui.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {
    // Widget Management
    private val _widgets = MutableStateFlow<List<WidgetState>>(emptyList())
    val widgets: StateFlow<List<WidgetState>> = _widgets.asStateFlow()

    // UI State
    private val _showSidebar = MutableStateFlow(false)
    val showSidebar: StateFlow<Boolean> = _showSidebar.asStateFlow()

    private val _showGridMenu = MutableStateFlow(false)
    val showGridMenu: StateFlow<Boolean> = _showGridMenu.asStateFlow()

    // System Stats
    private val _systemStats = MutableStateFlow(SystemStats())
    val systemStats: StateFlow<SystemStats> = _systemStats.asStateFlow()

    // Creative Tips
    private val creativeTips = listOf(
        "Neon heart + text widget = ❤️ message!",
        "Stack multiple decals for depth.",
        "Try a subtle glow on your clock.",
        "Make your battery % match Aura's mood!",
        "Use offsets to create a parallax effect.",
        "Experiment with different widget sizes.",
        "Try different neon colors for different moods.",
        "Create a custom widget layout for your home screen."
    )
    
    private val _currentTipWidget = MutableStateFlow<WidgetState?>(null)
    val currentTipWidget: StateFlow<WidgetState?> = _currentTipWidget.asStateFlow()

    // Glow Effect
    private val _currentGlowEffect = MutableStateFlow<GlowType?>(null)
    val currentGlowEffect: StateFlow<GlowType?> = _currentGlowEffect.asStateFlow()
    
    // Kai's Toolbox State
    private val _kaiToolboxVisible = MutableStateFlow(false)
    val kaiToolboxVisible: StateFlow<Boolean> = _kaiToolboxVisible.asStateFlow()
    
    private var tipCycleJob: Job? = null
    private var statsMonitoringJob: Job? = null

    init {
        // Initialize with some default widgets
        _widgets.value = listOf(
            WidgetState(
                id = generateUniqueId(),
                type = WidgetType.CLOCK,
                offset = Offset(100f, 100f),
                config = WidgetConfig(text = "10:10 AM", color = NeonTeal)
            ),
            WidgetState(
                id = generateUniqueId(),
                type = WidgetType.BATTERY,
                offset = Offset(100f, 200f),
                config = WidgetConfig(text = "88%", color = NeonGreen)
            )
        )
        startCreativeTipCycle()
        startSystemStatsMonitoring()
    }

    private fun generateUniqueId(): Int {
        return UUID.randomUUID().hashCode()
    }

    // --- Widget Management ---
    fun addWidget(
        type: WidgetType,
        initialOffset: Offset = Offset(200f, 200f),
        initialConfig: WidgetConfig = WidgetConfig()
    ) {
        val newWidget = WidgetState(
            id = generateUniqueId(),
            type = type,
            offset = initialOffset,
            isVisible = true,
            config = initialConfig.copy(color = initialConfig.color ?: NeonTeal)
        )
        _widgets.value = _widgets.value + newWidget
    }

    fun updateWidgetOffset(widgetId: Int, newOffset: Offset) {
        _widgets.value = _widgets.value.map {
            if (it.id == widgetId) it.copy(offset = newOffset) else it
        }
    }

    fun updateWidgetConfig(widgetId: Int, newConfig: WidgetConfig) {
        _widgets.value = _widgets.value.map {
            if (it.id == widgetId) it.copy(config = newConfig) else it
        }
    }

    fun removeWidget(widgetId: Int) {
        _widgets.value = _widgets.value.filterNot { it.id == widgetId }
    }

    fun toggleWidgetVisibility(widgetId: Int) {
        _widgets.value = _widgets.value.map {
            if (it.id == widgetId) it.copy(isVisible = !it.isVisible) else it
        }
    }

    // --- Overlay UI State ---
    fun toggleOverlaySidebar() {
        _showSidebar.value = !_showSidebar.value
        if (_showSidebar.value) {
            triggerOverlayGlow(GlowType.SIDEBAR_TOGGLE)
        }
    }

    fun setGridMenuVisibility(show: Boolean) {
        _showGridMenu.value = show
    }

    // --- Glow Effects for Overlay UI ---
    private fun triggerOverlayGlow(type: GlowType) {
        viewModelScope.launch {
            _currentGlowEffect.value = type
            delay(700) // Duration of glow effect
            _currentGlowEffect.value = null
        }
    }

    // --- Creative Tips ---
    private fun startCreativeTipCycle() {
        tipCycleJob?.cancel()
        tipCycleJob = viewModelScope.launch {
            while (true) {
                delay(20000L) // Time between tip appearances
                if (_currentTipWidget.value == null) { // Only if no tip is currently shown
                    val tipText = creativeTips.random()
                    val tipWidget = WidgetState(
                        id = generateUniqueId(),
                        type = WidgetType.CREATIVE_SPARK_TIP,
                        offset = Offset(50f, 800f), // Example position
                        isVisible = true,
                        config = WidgetConfig(
                            text = "💡 Aura: $tipText",
                            color = NeonPink,
                            size = WidgetSize.MEDIUM
                        )
                    )
                    _currentTipWidget.value = tipWidget
                    triggerOverlayGlow(GlowType.CREATIVE_SPARK)


                    delay(7000L) // How long the tip stays visible
                    _currentTipWidget.value = null // Hide tip
                }
            }
        }
    }


    // --- System Stats Monitoring (Mocked) ---
    private fun startSystemStatsMonitoring() {
        statsMonitoringJob?.cancel()
        statsMonitoringJob = viewModelScope.launch {
            while (true) {
                _systemStats.value = SystemStats(
                    cpuUsage = (10..70).random().toFloat(),
                    ramUsage = (30..80).random().toFloat(),
                    batteryLevel = (15..99).random(),
                    isCharging = listOf(true, false).random()
                )
                delay(5000) // Update stats every 5 seconds
            }
        }
    }


    // --- Kai's Toolbox ---
    fun toggleKaiToolbox() {
        _kaiToolboxVisible.value = !_kaiToolboxVisible.value
    }


    override fun onCleared() {
        super.onCleared()
        tipCycleJob?.cancel()
        statsMonitoringJob?.cancel()
    }
}
