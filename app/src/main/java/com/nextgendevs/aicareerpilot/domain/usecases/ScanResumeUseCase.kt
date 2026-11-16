package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.TextRecognitionRepository
import javax.inject.Inject

/** Turns the captured page photos of a paper resume into plain text. */
class ScanResumeUseCase @Inject constructor(
    private val repository: TextRecognitionRepository
) {
    suspend operator fun invoke(pagePaths: List<String>): String =
        repository.recognizeText(pagePaths).trim()
}
