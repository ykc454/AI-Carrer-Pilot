package com.nextgendevs.aicareerpilot.data.model.gemini_response

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/** Local sync state of a row. Room stores enums by name (TEXT). */
enum class SyncStatus {
    /** Created/changed locally and not yet pushed to the cloud. */
    PENDING,

    /** Local copy matches the cloud copy. */
    SYNCED
}

@Entity(
    tableName = "analysis_records",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawText: String,        // The text extracted from PDF
    val aiFeedback: String,     // Gemini analysis
    val resumeScore: Int,      // A 0-100 score given by the AI
    val aiMatchScore: Int,

    // ---------- Offline-first sync metadata ----------

    /** Stable id shared by every device; used as the Firestore document id. */
    @ColumnInfo(defaultValue = "")
    val remoteId: String = UUID.randomUUID().toString(),

    /** Last modification time; drives last-write-wins conflict resolution. */
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(defaultValue = "PENDING")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    /** Tombstone: hidden from UI, kept until the deletion reaches the cloud. */
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false
)
