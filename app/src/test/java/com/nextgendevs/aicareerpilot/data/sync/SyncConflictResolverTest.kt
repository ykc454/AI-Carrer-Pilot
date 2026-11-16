package com.nextgendevs.aicareerpilot.data.sync

import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.nextgendevs.aicareerpilot.data.model.gemini_response.SyncStatus
import com.nextgendevs.aicareerpilot.data.sync.SyncConflictResolver.Action
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncConflictResolverTest {

    private fun record(id: Int = 0, updatedAt: Long, status: SyncStatus = SyncStatus.SYNCED) =
        AnalysisRecord(
            id = id,
            remoteId = "r1",
            fileName = "cv.pdf",
            rawText = "",
            aiFeedback = "",
            resumeScore = 70,
            aiMatchScore = 70,
            updatedAt = updatedAt,
            syncStatus = status
        )

    @Test
    fun `new remote record is inserted`() {
        val remote = record(updatedAt = 10)
        assertEquals(Action.ApplyRemote(remote), SyncConflictResolver.resolve(null, remote, false))
    }

    @Test
    fun `unknown remote tombstone is ignored`() {
        assertEquals(Action.KeepLocal, SyncConflictResolver.resolve(null, record(updatedAt = 10), true))
    }

    @Test
    fun `newer remote overwrites local and keeps local primary key`() {
        val local = record(id = 7, updatedAt = 10)
        val remote = record(updatedAt = 20)
        val action = SyncConflictResolver.resolve(local, remote, false)
        assertEquals(Action.ApplyRemote(remote.copy(id = 7)), action)
    }

    @Test
    fun `newer local pending edit wins over older remote`() {
        val local = record(id = 7, updatedAt = 30, status = SyncStatus.PENDING)
        assertEquals(Action.KeepLocal, SyncConflictResolver.resolve(local, record(updatedAt = 20), false))
    }

    @Test
    fun `newer remote tombstone deletes local`() {
        val local = record(id = 7, updatedAt = 10)
        assertEquals(Action.DeleteLocal("r1"), SyncConflictResolver.resolve(local, record(updatedAt = 20), true))
    }
}
