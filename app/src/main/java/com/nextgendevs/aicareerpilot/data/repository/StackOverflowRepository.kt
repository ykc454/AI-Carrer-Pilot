package com.nextgendevs.aicareerpilot.data.repository

import com.nextgendevs.aicareerpilot.data.local.QuestionDao
import com.nextgendevs.aicareerpilot.data.local.toEntity
import com.nextgendevs.aicareerpilot.data.local.toQuestion
import com.nextgendevs.aicareerpilot.data.model.news.Question
import com.nextgendevs.aicareerpilot.data.remote.StackOverflowApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first: the UI always reads from Room; the network only refreshes the cache.
 */
@Singleton
class StackOverflowRepository @Inject constructor(
    private val api: StackOverflowApi,
    private val questionDao: QuestionDao
) {

    fun observeQuestions(): Flow<List<Question>> =
        questionDao.observeQuestions().map { list -> list.map { it.toQuestion() } }

    /** Time of the last successful refresh, or null if the cache is empty. */
    suspend fun lastUpdated(): Long? = questionDao.lastCachedAt()

    /** Fetches fresh data and atomically replaces the cache. Throws on network failure. */
    suspend fun refresh() {
        val now = System.currentTimeMillis()
        val fresh = api.getHotQuestions().items
        questionDao.replaceAll(fresh.mapIndexed { index, q -> q.toEntity(index, now) })
    }
}
