package com.nextgendevs.aicareerpilot.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nextgendevs.aicareerpilot.data.repository.StackOverflowRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val MAX_ATTEMPTS = 5

/** Pushes local history changes to Firestore and pulls remote ones. */
@HiltWorker
class HistorySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val engine: HistorySyncEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        engine.sync()
        Result.success()
    } catch (e: Exception) {
        Log.w("HistorySyncWorker", "Sync attempt $runAttemptCount failed", e)
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
    }
}

/** Refreshes the cached Job Market feed in the background so it opens instantly offline. */
@HiltWorker
class QuestionsRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: StackOverflowRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        repository.refresh()
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
    }
}
