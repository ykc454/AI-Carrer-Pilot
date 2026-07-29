package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.ResumeRepository
import javax.inject.Inject

class AnalyzeResumeUseCase @Inject constructor(
    private val repository: ResumeRepository
) {
    suspend operator fun invoke(
        fileName: String,
        resumeText: String,
        jd: String
    ): String {
        return repository.analyzeWithJD(fileName, resumeText, jd)
    }
}