package dev.aurakai.auraframefx.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.MainActivity
import dev.aurakai.auraframefx.R
import dev.aurakai.auraframefx.ui.kai.KaiNotchOrbComposable
import dev.aurakai.auraframefx.ui.kai.KaiStatus
import dev.aurakai.auraframefx.viewmodel.KaiViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class KaiOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        const val ACTION_START_OVERLAY = "dev.aurakai.auraframefx.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "dev.aurakai.auraframefx.action.STOP_OVERLAY"
        const val ACTION_OPEN_KAI_TOOLBOX = "dev.aurakai.auraframefx.action.OPEN_KAI_TOOLBOX"
        
        private const val NOTIFICATION_CHANNEL_ID = "KaiOverlayServiceChannel"
        private const val NOTIFICATION_ID = 1
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
        
        fun start(context: Context) {
            val intent = Intent(context, KaiOverlayService::class.java).apply {
                action = ACTION_START_OVERLAY
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, KaiOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.stopService(intent)
        }
        
        fun isRunning(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION") // getRunningServices is deprecated but still needed for our use case
            return activityManager.getRunningServices(Integer.MAX_VALUE).any {
                it.service.className == KaiOverlayService::class.name
            }
        }
        
        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Kai Overlay Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows Kai's overlay on top of other apps"
                }
                
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, KaiOverlayService::class.java))
        }
        
        fun hasOverlayPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        fun requestOverlayPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
                activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: KaiViewModel
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var savedStateRegistryController: SavedStateRegistryController
    
    private var isOverlayShown = false
    
    private val overlayTouchListener = View.OnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Handle touch events if needed
                true
            }
            else -> false
        }
    }

    override val lifecycle: Lifecycle
    
    init {
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        viewModel = ViewModelProvider(this, viewModelFactory)[KaiViewModel::class.java]
        
        // Initialize wake lock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AuraFrameFX::KaiOverlayWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
        
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("KaiOverlayService", "Service started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_OPEN_KAI_TOOLBOX -> {
                // This will be handled by the overlay view
            }
        }
        
        // Initialize the view model if not already done
        if (viewModel == null) {
            viewModel = ViewModelProvider(this)[KaiViewModel::class.java]
        }
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Check for overlay permission
        if (!hasOverlayPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (!isOverlayShown) {
            showOverlay()
        }
        
        return START_STICKY
    }

    private fun startForeground() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Kai Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kai Overlay Service is running"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(this)
        }
        
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_KAI_TOOLBOX
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, KaiOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.kai_status_monitoring))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_notification,
                getString(R.string.stop_overlay),
                stopPendingIntent
            )
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showOverlay() {
        if (isOverlayShown) return
        
        if (!hasOverlayPermission(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create and add the overlay view
        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AuraTheme {
                    KaiNotchOrbComposable(
                        onOrbClick = { handleOrbClick() },
                        status = viewModel.currentStatus
                    )
                }
            }
            setOnTouchListener(overlayTouchListener)
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 100
        }

        try {
            windowManager.addView(composeView, windowParams)
            isOverlayShown = true
            viewModel.updateStatus(KaiStatus.Monitoring)
        } catch (e: Exception) {
            Log.e("KaiOverlayService", "Failed to add overlay view", e)
            stopSelf()
        }
    }

    private fun handleOrbClick() {
        // Handle orb click - open Kai's Toolbox
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "dev.aurakai.auraframefx.ACTION_OPEN_KAI_TOOLBOX"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun removeOverlay() {
        if (!isOverlayShown) return
        
        try {
            composeView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            Log.e("KaiOverlayService", "Error removing overlay view", e)
        }
        composeView = null
        isOverlayShown = false
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        serviceScope.cancel()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
        get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val ACTION_SHOW_KAI_ORB = "dev.aurakai.auraframefx.ACTION_SHOW_KAI_ORB"
        const val ACTION_HIDE_KAI_ORB = "dev.aurakai.auraframefx.ACTION_HIDE_KAI_ORB"
        const val ACTION_UPDATE_KAI_STATUS = "dev.aurakai.auraframefx.ACTION_UPDATE_KAI_STATUS"
        const val EXTRA_KAI_STATUS = "extra_kai_status"
        const val EXTRA_KAI_MESSAGE = "extra_kai_message"
        const val ACTION_OPEN_KAI_TOOLBOX = "dev.aurakai.auraframefx.ACTION_OPEN_KAI_TOOLBOX"

        fun startService(context: Context) {
            val intent = Intent(context, KaiOverlayService::class.java).apply {
                action = ACTION_SHOW_KAI_ORB
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, KaiOverlayService::class.java).apply {
                action = ACTION_HIDE_KAI_ORB
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        viewModel = ViewModelProvider(this, viewModelFactory)[KaiViewModel::class.java]
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun addOverlayViewIfPermitted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        if (composeView == null) {
            composeView = ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                
                setContent {
                    val uiState by viewModel.uiState.observeAsState()
                    
                    uiState?.let { state ->
                        if (state.isEnabled) {
                            KaiNotchOrbComposable(
                                currentStatus = state.currentStatus,
                                currentText = state.currentText,
                                userXOffset = state.userXOffsetDp,
                                userYOffset = state.userYOffsetDp,
                                onClick = { viewModel.onOrbClicked() },
                                onLongPress = { openKaiToolbox() }
                            )
                        }
                    }
                }
            }


            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            windowParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 100 // Default Y position
            }

            try {
                windowManager.addView(composeView, windowParams)
            } catch (e: Exception) {
                stopSelf()
            }
        }
    }


    private fun removeOverlayView() {
        composeView?.let {
            try {
                windowManager.removeView(it)
                it.disposeComposition()
            } catch (e: Exception) {
                // Handle exception
            }
            composeView = null
            windowParams = null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_KAI_ORB -> addOverlayViewIfPermitted()
            ACTION_HIDE_KAI_ORB -> {
                removeOverlayView()
                stopSelf()
            }
            ACTION_UPDATE_KAI_STATUS -> {
                val statusString = intent.getStringExtra(EXTRA_KAI_STATUS)
                val message = intent.getStringExtra(EXTRA_KAI_MESSAGE)
                statusString?.let {
                    try {
                        val status = enumValueOf<KaiStatus>(it)
                        viewModel.updateStatus(status, message)
                    } catch (e: IllegalArgumentException) {
                        // Handle invalid status
                    }
                }
            }
            ACTION_OPEN_KAI_TOOLBOX -> openKaiToolbox()
        }
        return START_STICKY
    }

    private fun openKaiToolbox() {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_KAI_TOOLBOX
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add any necessary extras here
        }
        
        // Start the activity with animation
        startActivity(intent)
        
        // Add a small delay to ensure the activity is started
        android.os.Handler(mainLooper).postDelayed({
            // Update the status to show toolbox is open
            viewModel.updateStatus(KaiStatus.Information, "Toolbox opened")
        }, 300)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle configuration changes if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
