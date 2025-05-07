package dev.aurakai.auraframefx

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.service.KaiOverlayService
import dev.aurakai.auraframefx.ui.AuraNavHost
import dev.aurakai.auraframefx.ui.theme.AuraFrameFXTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                R.string.overlay_permission_granted,
                Toast.LENGTH_SHORT
            ).show()
            startKaiOverlayService()
        } else {
            Toast.makeText(
                this,
                R.string.overlay_permission_denied,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle the intent that started this activity
        handleIntent(intent)
        
        setContent {
            var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission()) }
            
            // Periodically check overlay permission status
            LaunchedEffect(Unit) {
                while (true) {
                    delay(5000) // Check every 5 seconds
                    hasOverlayPermission = checkOverlayPermission()
                }
            }
            
            // Check if we were launched from a deep link or notification
            val isKaiToolbox = intent?.action == KaiOverlayService.ACTION_OPEN_KAI_TOOLBOX
            
            AuraFrameFXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isKaiToolbox) {
                        KaiToolboxScreen(
                            hasOverlayPermission = hasOverlayPermission,
                            onRequestPermission = { requestOverlayPermission() },
                            onStartOverlay = { startKaiOverlayService() },
                            onStopOverlay = { stopKaiOverlayService() },
                            onBack = { finish() }
                        )
                    } else {
                        AuraNavHost()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Handle any deep links or intents here if needed
    }
    
    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }
    
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        requestOverlayPermissionLauncher.launch(intent)
    }
    
    private fun startKaiOverlayService() {
        if (checkOverlayPermission()) {
            KaiOverlayService.start(this)
            Toast.makeText(this, R.string.overlay_starting, Toast.LENGTH_SHORT).show()
        } else {
            requestOverlayPermission()
        }
    }
    
    private fun stopKaiOverlayService() {
        KaiOverlayService.stop(this)
        Toast.makeText(this, R.string.overlay_stopping, Toast.LENGTH_SHORT).show()
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AuraFrameFXApp(initialDeepLink: String? = null) {
    val context = LocalContext.current
    var hasOverlayPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || 
            Settings.canDrawOverlays(context)
        )
    }
    
    // Periodically check permission status
    LaunchedEffect(Unit) {
        while (true) {
            val currentPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || 
                                Settings.canDrawOverlays(context)
            if (currentPermission != hasOverlayPermission) {
                hasOverlayPermission = currentPermission
            }
            kotlinx.coroutines.delay(5000) // Check every 5 seconds
        }
    }
    
    AuraFrameFXTheme {
        if (initialDeepLink == "kai_toolbox") {
            // Show the Kai Toolbox screen
            KaiToolboxScreen(
                hasOverlayPermission = hasOverlayPermission,
                onRequestPermission = { (context as? MainActivity)?.requestOverlayPermission() },
                onStartOverlay = { (context as? MainActivity)?.startKaiOverlayService() },
                onStopOverlay = { (context as? MainActivity)?.stopKaiOverlayService() }
            )
        } else {
            // Show the main app navigation
            AuraNavHost(initialDeepLink)
        }
    }
}
