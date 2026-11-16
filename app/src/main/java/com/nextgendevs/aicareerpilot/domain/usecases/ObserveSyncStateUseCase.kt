package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.SyncRepository
import com.nextgendevs.aicareerpilot.domain.repository.SyncState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSyncStateUseCase @Inject constructor(
    private val repository: SyncRepository
) {
    operator fun invoke(): Flow<SyncState> = repository.observeSyncState()
}
