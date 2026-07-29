package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.UsageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanAnalyzeUseCaseTest {

    private val repository =
        mockk<UsageRepository>()

    @Test
    fun returns_true_when_analysis_is_allowed() = runTest {

        coEvery {
            repository.canAnalyze()
        } returns true

        val useCase =
            CanAnalyzeUseCase(repository)

        val result =
            useCase()

        assertTrue(result)
    }

    @Test
    fun returns_false_when_analysis_is_not_allowed() = runTest {

        coEvery {
            repository.canAnalyze()
        } returns false

        val useCase =
            CanAnalyzeUseCase(repository)

        val result =
            useCase()

        assertFalse(result)
    }
}