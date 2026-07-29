package com.nextgendevs.aicareerpilot.data.local
import androidx.room.*
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AnalysisRecord)

    @Query("SELECT * FROM analysis_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Delete
    suspend fun deleteRecord(record: AnalysisRecord)
}