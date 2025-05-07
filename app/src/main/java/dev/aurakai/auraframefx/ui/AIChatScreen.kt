package dev.aurakai.auraframefx.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.aurakai.auraframefx.ui.theme.NeonPurple
import dev.aurakai.auraframefx.ui.theme.NeonTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(onNavigateBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf("Welcome to AI Chat!")) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            chatHistory.forEach {
                Text(
                    it,
                    color = NeonTeal,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Type a message", color = NeonTeal) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonTeal,
                    unfocusedBorderColor = NeonPurple,
                    focusedLabelColor = NeonTeal,
                    unfocusedLabelColor = NeonPurple,
                    cursorColor = NeonTeal,
                    textColor = Color.White
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        chatHistory = chatHistory + "You: $message" + "\nAI: [response here]"
                        message = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
            ) {
                Text("Send", color = Color.Black)
            }
        }
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
        ) {
            Text("Back to Menu", color = Color.White)
        }
    }
}
