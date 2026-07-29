package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.ResumeRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    operator fun invoke() = repository.getHistory()
}