package com.example.aicareerpilot.domain.usecases

import com.example.aicareerpilot.domain.repository.UsageRepository
import javax.inject.Inject

class CanAnalyzeUseCase @Inject constructor(
    private val repository: UsageRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.canAnalyze()
    }
}