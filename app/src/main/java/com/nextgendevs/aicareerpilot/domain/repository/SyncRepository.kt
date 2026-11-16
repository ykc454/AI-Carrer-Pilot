package com.nextgendevs.aicareerpilot.domain.repository

import kotlinx.coroutines.flow.Flow

/** What the UI needs to know about background sync. */
sealed interface SyncState {
    /** Everything is in the cloud. */
    data object Synced : SyncState

    /** A sync is currently running. */
    data object Syncing : SyncState

    /** Local changes exist and are queued (usually waiting for network). */
    data class Pending(val pendingCount: Int) : SyncState

    /** Last attempt failed permanently; will retry on next trigger. */
    data class Failed(val pendingCount: Int) : SyncState
}

interface SyncRepository {

    /** Enqueue a one-off sync (runs when the device is online). */
    fun requestSync()

    /** Register the periodic background sync jobs. Idempotent. */
    fun schedulePeriodicSync()

    fun observeSyncState(): Flow<SyncState>

    /** Cancel all sync work and wipe the user's local data (used on logout). */
    suspend fun clearLocalData()
}
