package com.example.aicareerpilot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_records")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String,        // The text extracted from PDF
    val aiFeedback: String,     // Gemini analysis
    val resumeScore: Int        // A 0-100 score given by the AI
)