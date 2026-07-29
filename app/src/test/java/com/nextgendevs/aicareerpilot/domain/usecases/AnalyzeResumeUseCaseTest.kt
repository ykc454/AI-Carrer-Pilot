package com.nextgendevs.aicareerpilot.domain.usecases

import com.nextgendevs.aicareerpilot.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzeResumeUseCaseTest {

    private val repository =
        mockk<ResumeRepository>()

    @Test
    fun returns_analysis_result_from_repository() = runTest {

        val expected =
            "Analysis Complete"

        coEvery {
            repository.analyzeWithJD(
                any(),
                any(),
                any()
            )
        } returns expected

        val useCase =
            AnalyzeResumeUseCase(repository)

        val result =
            useCase(
                fileName = "resume.pdf",
                resumeText = "Android Developer Resume",
                jd = "Android Developer Job Description"
            )

        assertEquals(
            expected,
            result
        )
    }

    @Test
    fun returns_empty_string_when_repository_returns_empty_result() = runTest {

        coEvery {
            repository.analyzeWithJD(
                any(),
                any(),
                any()
            )
        } returns ""

        val useCase =
            AnalyzeResumeUseCase(repository)

        val result =
            useCase(
                "resume.pdf",
                "resume text",
                "job description"
            )

        assertEquals("", result)
    }
}