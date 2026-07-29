package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.UsageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRemainingAttemptsUseCaseTest {

    private val repository =
        mockk<UsageRepository>()

    @Test
    fun returns_remaining_attempts_correctly() = runTest {

        coEvery {
            repository.getRemainingAttempts()
        } returns 2

        val useCase =
            GetRemainingAttemptsUseCase(repository)

        val result =
            useCase()

        assertEquals(2, result)
    }

    @Test
    fun returns_zero_when_no_attempts_left() = runTest {

        coEvery {
            repository.getRemainingAttempts()
        } returns 0

        val useCase =
            GetRemainingAttemptsUseCase(repository)

        val result =
            useCase()

        assertEquals(0, result)
    }
}