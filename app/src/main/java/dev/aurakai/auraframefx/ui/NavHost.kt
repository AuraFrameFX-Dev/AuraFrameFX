package dev.aurakai.auraframefx.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.aurakai.auraframefx.service.KaiOverlayService
import dev.aurakai.auraframefx.ui.kai.KaiToolboxScreen

@Composable
fun AuraNavHost(initialDeepLink: String? = null) {
    val navController = rememberNavController()
    
    // Handle initial deep link if provided
    LaunchedEffect(initialDeepLink) {
        initialDeepLink?.let { route ->
            navController.navigate(route) {
                // Clear back stack to prevent going back to splash/loading screen
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Handle deep links and intents
    LaunchedEffect(Unit) {
        // Handle navigation from intents
        navController.handleDeepLink(androidx.compose.ui.platform.LocalContext.current.intent)
    }
    
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToEcosystem = { navController.navigate("ecosystem") },
                onNavigateToKaiToolbox = { navController.navigate("kai_toolbox") }
            )
        }
        composable("chat") {
            AIChatScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("ecosystem") {
            EcosystemMenuScreen()
        }
        composable("kai_toolbox") {
            KaiToolboxScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Extension function to handle deep links
private fun NavHostController.handleDeepLink(intent: android.content.Intent) {
    when (intent.action) {
        KaiOverlayService.ACTION_OPEN_KAI_TOOLBOX -> {
            navigate("kai_toolbox") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
