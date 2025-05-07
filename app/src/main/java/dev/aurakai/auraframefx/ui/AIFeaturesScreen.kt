package dev.aurakai.auraframefx.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.ai.AuraAIService
import kotlinx.coroutines.launch

@Composable
fun AIFeaturesScreen(
    backendUrl: String = "https://YOUR_CLOUD_RUN_URL", // TODO: Provide actual URL
    idToken: () -> String = { "" }, // TODO: Provide actual token
) {
    val coroutineScope = rememberCoroutineScope()
    var response by remember { mutableStateOf<Result<String>>(Result.success("")) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "AI Features",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Example Feature Button 1: Text Generation
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = AuraAIService(backendUrl).generateText(
                            prompt = "Explain quantum computing in simple terms",
                            idToken = idToken()
                        )
                        response = Result.success(result)
                    } catch (e: Exception) {
                        response = Result.failure(e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Generate Text")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Example Feature Button 2: Image Generation
        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val result = AuraAIService(backendUrl).generateImage(
                            prompt = "A futuristic city at night with neon lights",
                            idToken = idToken()
                        )
                        response = Result.success("Image generated: $result")
                    } catch (e: Exception) {
                        response = Result.failure(e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Generate Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display response or loading state
        when {
            isLoading -> Text("Processing...")
            response.isSuccess -> Text(response.getOrNull() ?: "No response")
            else -> Text("Error: ${response.exceptionOrNull()?.message}")
        }
    }
}
