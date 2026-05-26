package com.example.aicareerpilot.domain.usecases

import com.example.aicareerpilot.domain.model.AnalysisRecord
import com.example.aicareerpilot.domain.repository.ResumeRepository
import javax.inject.Inject

class DeleteRecordUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    suspend operator fun invoke(record: AnalysisRecord) {
        repository.deleteRecord(record)
    }
}