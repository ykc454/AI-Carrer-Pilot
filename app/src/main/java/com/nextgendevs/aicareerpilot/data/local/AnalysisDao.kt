package com.nextgendevs.aicareerpilot.data.local
import androidx.room.*
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.nextgendevs.aicareerpilot.data.model.gemini_response.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AnalysisRecord)

    /** UI stream: tombstoned rows are hidden immediately, even before they sync. */
    @Query("SELECT * FROM analysis_records WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Delete
    suspend fun deleteRecord(record: AnalysisRecord)

    // ---------------- Sync support ----------------

    /** Soft delete: mark as tombstone so the worker can propagate it to the cloud. */
    @Query(
        """UPDATE analysis_records
           SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :now
           WHERE remoteId = :remoteId"""
    )
    suspend fun markDeleted(remoteId: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM analysis_records WHERE syncStatus = 'PENDING'")
    suspend fun getPendingRecords(): List<AnalysisRecord>

    @Query("SELECT COUNT(*) FROM analysis_records WHERE syncStatus = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM analysis_records WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): AnalysisRecord?

    /**
     * Marks a row synced only if it was not modified while the upload was in flight
     * (guarded by updatedAt), so a concurrent local edit is never lost.
     */
    @Query(
        """UPDATE analysis_records SET syncStatus = :status
           WHERE remoteId = :remoteId AND updatedAt = :updatedAt"""
    )
    suspend fun markSynced(
        remoteId: String,
        updatedAt: Long,
        status: SyncStatus = SyncStatus.SYNCED
    )

    @Query("DELETE FROM analysis_records WHERE remoteId = :remoteId")
    suspend fun hardDelete(remoteId: String)

    @Query("DELETE FROM analysis_records")
    suspend fun clearAll()
}
