package com.example.aicareerpilot.domain.repository

import com.example.aicareerpilot.data.model.gemini_response.AnalysisRecord
import kotlinx.coroutines.flow.Flow

interface ResumeRepository {

    suspend fun analyzeWithJD(
        fileName: String,
        resumeText: String,
        jobDescription: String
    ): String

    fun getHistory(): Flow<List<AnalysisRecord>>

    suspend fun deleteRecord(record: AnalysisRecord)
}