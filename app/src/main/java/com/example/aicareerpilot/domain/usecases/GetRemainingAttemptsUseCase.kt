package com.example.aicareerpilot.domain.usecases

import com.example.aicareerpilot.domain.repository.UsageRepository
import javax.inject.Inject

class GetRemainingAttemptsUseCase @Inject constructor(
    private val repository: UsageRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getRemainingAttempts()
    }
}