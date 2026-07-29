package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.UsageRepository
import javax.inject.Inject

class GetRemainingAttemptsUseCase @Inject constructor(
    private val repository: UsageRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getRemainingAttempts()
    }
}