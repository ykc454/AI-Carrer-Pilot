package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.SyncRepository
import javax.inject.Inject

class SyncHistoryUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    operator fun invoke() = repository.requestSync()
}
