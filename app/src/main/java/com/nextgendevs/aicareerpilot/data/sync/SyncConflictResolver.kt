package com.nextgendevs.aicareerpilot.data.sync

import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord

/**
 * Pure last-write-wins merge rules. Kept free of Android/Firebase types so it is unit-testable.
 */
object SyncConflictResolver {

    sealed interface Action {
        /** Write the remote version into Room (insert or overwrite). */
        data class ApplyRemote(val record: AnalysisRecord) : Action

        /** Remote is a tombstone that wins: remove the local row. */
        data class DeleteLocal(val remoteId: String) : Action

        /** Local is newer (or equal) – keep it; it will be/has been pushed. */
        data object KeepLocal : Action
    }

    /**
     * @param local  current Room row for this remoteId, or null
     * @param remote record decoded from Firestore
     * @param remoteDeleted whether the remote document is a tombstone
     */
    fun resolve(local: AnalysisRecord?, remote: AnalysisRecord, remoteDeleted: Boolean): Action {
        if (local == null) {
            return if (remoteDeleted) Action.KeepLocal // nothing to delete, nothing to add
            else Action.ApplyRemote(remote)
        }
        if (remote.updatedAt <= local.updatedAt) return Action.KeepLocal

        return if (remoteDeleted) Action.DeleteLocal(local.remoteId)
        else Action.ApplyRemote(remote.copy(id = local.id)) // keep local PK so nav routes stay valid
    }
}
