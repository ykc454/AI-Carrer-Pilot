package com.nextgendevs.aicareerpilot.data.sync

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nextgendevs.aicareerpilot.data.local.AnalysisDao
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.nextgendevs.aicareerpilot.data.model.gemini_response.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-way sync between Room (source of truth) and Firestore
 * at users/{uid}/analyses/{remoteId}.
 *
 * 1. PUSH  – every PENDING row (upserts and tombstones) is written to Firestore.
 * 2. PULL  – documents changed since the last pull are merged with last-write-wins.
 * 3. PRUNE – tombstones confirmed in the cloud are hard-deleted locally.
 */
@Singleton
class HistorySyncEngine @Inject constructor(
    @ApplicationContext context: Context,
    private val dao: AnalysisDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val prefs = context.getSharedPreferences("history_sync", Context.MODE_PRIVATE)

    /** @return false when there is no signed-in user (nothing to sync against). */
    suspend fun sync(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val collection = firestore.collection("users").document(uid).collection("analyses")

        // ---------- 1. PUSH ----------
        for (record in dao.getPendingRecords()) {
            withTimeout(NETWORK_TIMEOUT_MS) {
                collection.document(record.remoteId).set(record.toFirestoreMap()).await()
            }
            if (record.isDeleted) {
                // Tombstone now lives in the cloud for other devices; drop it locally.
                dao.hardDelete(record.remoteId)
            } else {
                dao.markSynced(record.remoteId, record.updatedAt)
            }
        }

        // ---------- 2. PULL (incremental) ----------
        val lastPullKey = "last_pull_$uid"
        val lastPull = prefs.getLong(lastPullKey, 0L)
        val snapshot = withTimeout(NETWORK_TIMEOUT_MS) {
            collection.whereGreaterThan(FIELD_UPDATED_AT, lastPull).get().await()
        }

        var newestSeen = lastPull
        for (doc in snapshot.documents) {
            val remote = doc.toAnalysisRecord() ?: continue
            val remoteDeleted = doc.getBoolean(FIELD_DELETED) == true
            newestSeen = maxOf(newestSeen, remote.updatedAt)

            val local = dao.getByRemoteId(remote.remoteId)
            when (val action = SyncConflictResolver.resolve(local, remote, remoteDeleted)) {
                is SyncConflictResolver.Action.ApplyRemote -> dao.insertRecord(action.record)
                is SyncConflictResolver.Action.DeleteLocal -> dao.hardDelete(action.remoteId)
                SyncConflictResolver.Action.KeepLocal -> Unit
            }
        }
        prefs.edit { putLong(lastPullKey, newestSeen) }
        return true
    }

    fun resetCursor() = prefs.edit { clear() }

    // ---------------- Mapping ----------------

    private fun AnalysisRecord.toFirestoreMap(): Map<String, Any> = mapOf(
        "fileName" to fileName,
        "timestamp" to timestamp,
        // Firestore docs are capped at 1 MiB; full text stays on-device.
        "rawText" to rawText.take(MAX_RAW_TEXT_CHARS),
        "aiFeedback" to aiFeedback,
        "resumeScore" to resumeScore,
        "aiMatchScore" to aiMatchScore,
        FIELD_UPDATED_AT to updatedAt,
        FIELD_DELETED to isDeleted
    )

    private fun DocumentSnapshot.toAnalysisRecord(): AnalysisRecord? {
        val updatedAt = getLong(FIELD_UPDATED_AT) ?: return null
        return AnalysisRecord(
            remoteId = id,
            fileName = getString("fileName") ?: "",
            timestamp = getLong("timestamp") ?: updatedAt,
            rawText = getString("rawText") ?: "",
            aiFeedback = getString("aiFeedback") ?: "",
            resumeScore = getLong("resumeScore")?.toInt() ?: 0,
            aiMatchScore = getLong("aiMatchScore")?.toInt() ?: 0,
            updatedAt = updatedAt,
            syncStatus = SyncStatus.SYNCED,
            isDeleted = false
        )
    }

    private companion object {
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_DELETED = "isDeleted"
        const val MAX_RAW_TEXT_CHARS = 200_000
        // Firestore's await() never fails offline (it waits); bound it so the worker can retry.
        const val NETWORK_TIMEOUT_MS = 30_000L
    }
}
