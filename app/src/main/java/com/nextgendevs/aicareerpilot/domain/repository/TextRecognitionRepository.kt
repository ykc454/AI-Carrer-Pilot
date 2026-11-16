package com.nextgendevs.aicareerpilot.domain.repository

/**
 * Reads text from photos of a paper resume.
 *
 * The domain layer only knows about image file paths and plain text, so it
 * stays free of CameraX / ML Kit types. The implementation lives in the data layer.
 */
interface TextRecognitionRepository {

    /**
     * Runs OCR on each page image (in order) and returns the combined text.
     * Pages are separated by a blank line.
     */
    suspend fun recognizeText(imagePaths: List<String>): String
}
