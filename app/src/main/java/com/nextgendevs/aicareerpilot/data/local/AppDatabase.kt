package com.nextgendevs.aicareerpilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord

@Database(entities = [AnalysisRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
}