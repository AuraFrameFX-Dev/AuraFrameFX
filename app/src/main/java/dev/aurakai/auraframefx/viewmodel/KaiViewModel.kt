package dev.aurakai.auraframefx.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.aurakai.auraframefx.R
import dev.aurakai.auraframefx.service.KaiOverlayService
import dev.aurakai.auraframefx.ui.kai.KaiStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KaiUiState(
    val isEnabled: Boolean = false,
    val currentStatus: KaiStatus = KaiStatus.Idle,
    val currentText: String? = null,
    val userXOffsetDp: Float = 0f,
    val userYOffsetDp: Float = 0f,
    val isDragging: Boolean = false,
    val showOverlayPermissionDialog: Boolean = false,
    val showBatteryOptimizationDialog: Boolean = false,
    val showNotificationAccessDialog: Boolean = false,
    val isServiceRunning: Boolean = false
)

@HiltViewModel
class KaiViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(KaiUiState())
    val uiState: StateFlow<KaiUiState> = _uiState.asStateFlow()

    private var statusResetJob: Job? = null
    
    val currentStatus: KaiStatus
        get() = _uiState.value.currentStatus

    init {
        checkAndStartKaiService()
    }
    
    fun updateStatus(newStatus: KaiStatus, text: String? = null) {
        _uiState.update { it.copy(currentStatus = newStatus, currentText = text) }
        
        // Auto-reset certain statuses after a delay
        when (newStatus) {
            KaiStatus.Information -> {
                statusResetJob?.cancel()
                statusResetJob = viewModelScope.launch {
                    delay(3000) // Reset after 3 seconds
                    if (_uiState.value.currentStatus == KaiStatus.Information) {
                        _uiState.update { it.copy(currentStatus = KaiStatus.Idle, currentText = null) }
                    }
                }
            }
            else -> {}
        }
    }
    
    fun toggleKaiService() {
        if (_uiState.value.isServiceRunning) {
            stopKaiService()
        } else {
            checkAndStartKaiService()
        }
    }

    fun checkAndStartKaiService() {
        if (KaiOverlayService.hasOverlayPermission(getApplication())) {
            startKaiService()
        } else {
            _uiState.update { it.copy(showOverlayPermissionDialog = true) }
        }
    }
    
    fun requestOverlayPermission() {
        _uiState.update { it.copy(showOverlayPermissionDialog = false) }
        val context = getApplication<Application>()
        KaiOverlayService.requestOverlayPermission(context as android.app.Activity)
    }
    
    fun onOverlayPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(showOverlayPermissionDialog = false) }
        if (granted) {
            startKaiService()
        }
    }

    fun startKaiService() {
        val context = getApplication<Application>()
        KaiOverlayService.start(context)
        _uiState.update { it.copy(isEnabled = true, isServiceRunning = true) }
    }

    fun stopKaiService() {
        val context = getApplication<Application>()
        KaiOverlayService.stop(context)
        _uiState.update { it.copy(isEnabled = false, isServiceRunning = false) }
    }
    
    fun updatePosition(x: Float, y: Float) {
        _uiState.update { it.copy(userXOffsetDp = x, userYOffsetDp = y) }
    }
    
    fun setDragging(isDragging: Boolean) {
        _uiState.update { it.copy(isDragging = isDragging) }
    }
    
    fun onBatteryOptimizationDialogDismissed() {
        _uiState.update { it.copy(showBatteryOptimizationDialog = false) }
    }
    
    fun onNotificationAccessDialogDismissed() {
        _uiState.update { it.copy(showNotificationAccessDialog = false) }
    }
    
    private fun checkBatteryOptimization() {
        val context = getApplication<Application>()
        val packageName = context.packageName
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                _uiState.update { it.copy(showBatteryOptimizationDialog = true) }
            }
        }
    }
    
    private fun checkNotificationAccess() {
        val context = getApplication<Application>()
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val packageName = context.packageName
        
        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            _uiState.update { it.copy(showNotificationAccessDialog = true) }
        }
    }
    
    fun openBatteryOptimizationSettings() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    fun openNotificationAccessSettings() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun onOverlayPermissionResult(granted: Boolean) {
        updateState { it.copy(showOverlayPermissionDialog = false) }
        if (granted) {
            startKaiService()
        }
    }

    private fun startKaiService() {
        updateState { it.copy(isEnabled = true) }
        KaiOverlayService.startService(getApplication())
        updateStatus(KaiStatus.Monitoring, getApplication<Application>().getString(R.string.kai_status_monitoring))
    }

    fun stopKaiService() {
        updateState { it.copy(isEnabled = false) }
        KaiOverlayService.stopService(getApplication())
        updateStatus(KaiStatus.Disabled)
    }

    fun updateStatus(newStatus: KaiStatus, message: String? = null) {
        viewModelScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    currentStatus = newStatus,
                    currentText = message ?: _uiState.value.currentText
                )
            )

            // Auto-reset status after delay if it's not a persistent state
            statusResetJob?.cancel()
            if (newStatus != KaiStatus.Idle && newStatus != KaiStatus.Disabled) {
                statusResetJob = viewModelScope.launch {
                    delay(5000) // 5 seconds
                    if (_uiState.value.currentStatus == newStatus) {
                        _uiState.emit(
                            _uiState.value.copy(
                                currentStatus = KaiStatus.Idle,
                                currentText = null
                            )
                        )
                    }
                }
            }
        }
    }

    fun onOrbClicked() {
        when (_uiState.value.currentStatus) {
            KaiStatus.Disabled -> startKaiService()
            else -> toggleKaiToolbox()
        }
    }

    fun toggleKaiToolbox() {
        val context = getApplication<Application>()
        val intent = Intent(context, KaiOverlayService::class.java).apply {
            action = KaiOverlayService.ACTION_OPEN_KAI_TOOLBOX
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }

    fun updatePosition(x: Float, y: Float) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(userXOffsetDp = x, userYOffsetDp = y))
        }
    }

    fun requestOverlayPermission() {
        val context = getApplication<Application>()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun updateState(update: (KaiUiState) -> KaiUiState) {
        viewModelScope.launch {
            _uiState.emit(update(_uiState.value))
        }
    }

    override fun onCleared() {
        super.onCleared()
        statusResetJob?.cancel()
    }
}
