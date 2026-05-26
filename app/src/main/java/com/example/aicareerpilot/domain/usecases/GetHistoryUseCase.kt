package com.example.aicareerpilot.domain.usecases

import com.example.aicareerpilot.domain.repository.ResumeRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    operator fun invoke() = repository.getHistory()
}