package dev.aurakai.auraframefx.domain.usecase

import dev.aurakai.auraframefx.domain.repository.AuraFrameRepository
import javax.inject.Inject

class GetWelcomeMessageUseCase @Inject constructor(
    private val repository: AuraFrameRepository,
) {
    suspend operator fun invoke(): String {
        return repository.getWelcomeMessage()
    }
}
