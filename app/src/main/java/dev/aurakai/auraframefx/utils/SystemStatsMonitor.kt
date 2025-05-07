package dev.aurakai.auraframefx.utils

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Process
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SystemStatsMonitor(private val context: Context) {
    private val _cpuUsage = MutableStateFlow(0f)
    private val _ramUsage = MutableStateFlow(0f)
    private val _batteryLevel = MutableStateFlow(0)
    private val _isCharging = MutableStateFlow(false)
    
    val cpuUsage: StateFlow<Float> = _cpuUsage.asStateFlow()
    val ramUsage: StateFlow<Float> = _ramUsage.asStateFlow()
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()
    
    private var lastCpuTime: Long = 0
    private var lastAppCpuTime: Long = 0
    private val cpuExecutor = Executors.newSingleThreadScheduledExecutor()
    
    init {
        startMonitoring()
    }
    
    private fun startMonitoring() {
        // Update stats every 2 seconds
        cpuExecutor.scheduleAtFixedRate({
            updateCpuUsage()
            updateRamUsage()
            updateBatteryStatus()
        }, 0, 2, TimeUnit.SECONDS)
    }
    
    private fun updateCpuUsage() {
        try {
            val pid = Process.myPid()
            val statFile = File("/proc/$pid/stat")
            
            if (statFile.exists()) {
                val reader = BufferedReader(FileReader(statFile))
                val stats = reader.readLine().split("\\s+".toRegex())
                reader.close()
                
                if (stats.size > 21) {
                    val utime = stats[13].toLong()
                    val stime = stats[14].toLong()
                    val cutime = stats[15].toLong()
                    val cstime = stats[16].toLong()
                    
                    val currentCpuTime = utime + stime + cutime + cstime
                    
                    if (lastCpuTime > 0) {
                        val cpuTimeDiff = currentCpuTime - lastAppCpuTime
                        val totalCpuTimeDiff = getTotalCpuTime() - lastCpuTime
                        
                        if (totalCpuTimeDiff > 0) {
                            val cpuUsage = (cpuTimeDiff * 100f / totalCpuTimeDiff).coerceIn(0f, 100f)
                            _cpuUsage.value = cpuUsage
                        }
                    }
                    
                    lastAppCpuTime = currentCpuTime
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    private fun getTotalCpuTime(): Long {
        try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine()
            reader.close()
            
            if (line.startsWith("cpu ")) {
                val parts = line.split("\\s+".toRegex())
                if (parts.size > 8) {
                    var total = 0L
                    for (i in 1..8) {
                        total += parts[i].toLong()
                    }
                    lastCpuTime = total
                    return total
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return 0
    }
    
    private fun updateRamUsage() {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalRam = memoryInfo.totalMem / (1024 * 1024) // Convert to MB
            val availableRam = memoryInfo.availMem / (1024 * 1024) // Convert to MB
            val usedRam = totalRam - availableRam
            
            val ramUsagePercentage = (usedRam * 100f / totalRam).coerceIn(0f, 100f)
            _ramUsage.value = ramUsagePercentage
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    private fun updateBatteryStatus() {
        try {
            val batteryStatus = ContextCompat.registerReceiver(
                context,
                null,
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            )
            
            batteryStatus?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = (level * 100 / scale.toFloat()).toInt()
                
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                
                _batteryLevel.value = batteryPct
                _isCharging.value = isCharging
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    fun destroy() {
        cpuExecutor.shutdownNow()
    }
}
