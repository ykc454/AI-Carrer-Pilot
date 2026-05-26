package com.example.aicareerpilot.domain.usecases

import com.example.aicareerpilot.domain.repository.ResumeRepository
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