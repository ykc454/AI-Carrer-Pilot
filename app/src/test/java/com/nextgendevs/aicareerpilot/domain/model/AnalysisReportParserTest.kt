package com.nextgendevs.aicareerpilot.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalysisReportParserTest {

    private val stored = """
        ##### Automated Keyword Evaluation
        **Matched Skills:** kotlin, compose
        **Keyword Match Density:** 33%

        MATCH_SCORE: 43

        MATCH_SUMMARY:
        - Strong Android basics
        - Limited backend exposure

        MATCHED_SKILLS:
        - Kotlin
        - Jetpack Compose

        **MISSING_KEYWORDS:**
        * Docker
        * CI/CD

        THINGS_TO_IMPROVE:
        1. Quantify impact
        2. Add a projects section

        ATS_OPTIMIZATION_TIPS:
        - Use standard headings

        FINAL_VERDICT:
        - Good junior fit, not senior yet.
    """.trimIndent()

    @Test
    fun `parses every section of the stored feedback`() {
        val r = AnalysisReportParser.parse(stored)

        assertEquals(43, r.aiScore)
        assertEquals(33, r.keywordDensity)
        assertEquals(listOf("kotlin", "compose"), r.keywordMatches)
        assertEquals(listOf("Strong Android basics", "Limited backend exposure"), r.summary)
        assertEquals(listOf("Kotlin", "Jetpack Compose"), r.matchedSkills)
        assertEquals(listOf("Docker", "CI/CD"), r.missingKeywords)
        assertEquals(listOf("Quantify impact", "Add a projects section"), r.improvements)
        assertEquals(listOf("Use standard headings"), r.atsTips)
        assertEquals("Good junior fit, not senior yet.", r.verdict)
    }

    @Test
    fun `offline fallback report keeps status and suggestions`() {
        val offline = """
            SYSTEM_STATUS:
            - No internet connection available.

            MISSING_KEYWORDS:
            - AI analysis unavailable

            IMPROVEMENT_SUGGESTIONS:
            - Retry once network/service stabilizes

            AI_MATCH_SCORE: 20
        """.trimIndent()

        val r = AnalysisReportParser.parse(offline)

        assertEquals(listOf("No internet connection available."), r.systemStatus)
        assertEquals(20, r.aiScore)
        assertEquals(listOf("Retry once network/service stabilizes"), r.improvements)
    }

    @Test
    fun `unknown text yields empty report`() {
        val r = AnalysisReportParser.parse("just some free text")
        assertTrue(r.isEmpty)
        assertNull(r.verdict)
    }
}
