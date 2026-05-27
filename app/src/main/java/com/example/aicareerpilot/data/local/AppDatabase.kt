package com.example.aicareerpilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aicareerpilot.data.model.gemini_response.AnalysisRecord

@Database(entities = [AnalysisRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
}