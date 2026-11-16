package com.nextgendevs.aicareerpilot.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.nextgendevs.aicareerpilot.data.local.AnalysisDao
import com.nextgendevs.aicareerpilot.data.sync.HistorySyncEngine
import com.nextgendevs.aicareerpilot.data.sync.HistorySyncWorker
import com.nextgendevs.aicareerpilot.data.sync.QuestionsRefreshWorker
import com.nextgendevs.aicareerpilot.domain.repository.SyncRepository
import com.nextgendevs.aicareerpilot.domain.repository.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val workManager: WorkManager,
    private val analysisDao: AnalysisDao,
    private val engine: HistorySyncEngine
) : SyncRepository {

    private val onlineConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<HistorySyncWorker>()
            .setConstraints(onlineConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(TAG_HISTORY)
            .addTag(TAG_ONE_TIME)
            .build()

        // APPEND_OR_REPLACE: if a sync is running, queue another one after it so
        // changes made mid-sync are not missed; a failed chain is replaced.
        workManager.enqueueUniqueWork(
            WORK_HISTORY_ONE_TIME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    override fun schedulePeriodicSync() {
        workManager.enqueueUniquePeriodicWork(
            WORK_HISTORY_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<HistorySyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(onlineConstraints)
                .addTag(TAG_HISTORY)
                .build()
        )

        workManager.enqueueUniquePeriodicWork(
            WORK_QUESTIONS_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<QuestionsRefreshWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        )
    }

    override fun observeSyncState(): Flow<SyncState> =
        combine(
            workManager.getWorkInfosByTagFlow(TAG_HISTORY),
            analysisDao.observePendingCount()
        ) { infos, pending ->
            when {
                infos.any { it.state == WorkInfo.State.RUNNING } -> SyncState.Syncing
                pending == 0 -> SyncState.Synced
                infos.any { TAG_ONE_TIME in it.tags && it.state == WorkInfo.State.FAILED } ->
                    SyncState.Failed(pending)
                else -> SyncState.Pending(pending)
            }
        }.distinctUntilChanged()

    override suspend fun clearLocalData() {
        workManager.cancelUniqueWork(WORK_HISTORY_ONE_TIME)
        workManager.cancelUniqueWork(WORK_HISTORY_PERIODIC)
        analysisDao.clearAll()
        engine.resetCursor()
    }

    private companion object {
        const val TAG_HISTORY = "history_sync"
        const val TAG_ONE_TIME = "history_sync_one_time_tag"
        const val WORK_HISTORY_ONE_TIME = "history_sync_one_time"
        const val WORK_HISTORY_PERIODIC = "history_sync_periodic"
        const val WORK_QUESTIONS_PERIODIC = "questions_refresh_periodic"
    }
}
