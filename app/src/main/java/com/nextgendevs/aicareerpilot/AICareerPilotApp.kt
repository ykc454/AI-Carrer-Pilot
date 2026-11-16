package com.nextgendevs.aicareerpilot

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nextgendevs.aicareerpilot.domain.repository.SyncRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AICareerPilotApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncRepository: SyncRepository

    /** Lets WorkManager construct @HiltWorker classes with injected dependencies. */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncRepository.schedulePeriodicSync()
        // Flush anything queued while the app was closed/offline.
        syncRepository.requestSync()
    }
}
