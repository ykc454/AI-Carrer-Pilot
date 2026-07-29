package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.nextgendevs.aicareerpilot.domain.repository.ResumeRepository
import javax.inject.Inject

class DeleteRecordUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    suspend operator fun invoke(record: AnalysisRecord) {
        repository.deleteRecord(record)
    }
}