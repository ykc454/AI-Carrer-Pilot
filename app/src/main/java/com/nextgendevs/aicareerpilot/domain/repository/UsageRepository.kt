package com.nextgendevs.aicareerpilot.domain.repository

interface UsageRepository {

    suspend fun canAnalyze(): Boolean

    suspend fun incrementUsage()

    suspend fun getRemainingAttempts(): Int
}