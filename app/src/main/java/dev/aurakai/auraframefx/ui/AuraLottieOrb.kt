package dev.aurakai.auraframefx.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun AuraLottieOrb(modifier: Modifier = Modifier) {
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.Asset("aura_orb.json"))
    val composition = compositionResult.value
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(120.dp)
    )
}
