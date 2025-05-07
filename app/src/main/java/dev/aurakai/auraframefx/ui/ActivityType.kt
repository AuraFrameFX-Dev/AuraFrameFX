package dev.aurakai.auraframefx.ui

enum class ActivityType {
    Idle, Typing, Scrolling, Clicking, VoiceInput
}

data class UserActivity(
    val type: ActivityType,
    val timestamp: Long = System.currentTimeMillis(),
)
