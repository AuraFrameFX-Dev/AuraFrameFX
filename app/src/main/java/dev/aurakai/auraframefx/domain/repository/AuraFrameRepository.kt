package dev.aurakai.auraframefx.domain.repository

interface AuraFrameRepository {
    // Define your repository methods here
    suspend fun getWelcomeMessage(): String
}
