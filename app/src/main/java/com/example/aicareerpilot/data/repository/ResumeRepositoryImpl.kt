package com.example.aicareerpilot.data.repository

import com.example.aicareerpilot.data.local.AnalysisDao
import com.example.aicareerpilot.domain.model.AnalysisRecord
import com.example.aicareerpilot.domain.repository.ResumeRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ResumeRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val analysisDao: AnalysisDao
): ResumeRepository {
    override suspend fun analyzeWithJD(
        fileName: String,
        resumeText: String,
        jobDescription: String
    ): String {

        val targetKeywords = extractKeywords(jobDescription)

        val matchedKeywords = targetKeywords.filter {
            resumeText.contains(it, ignoreCase = true)
        }

        val keywordScore =
            if (targetKeywords.isEmpty()) 0
            else (matchedKeywords.size * 100) / targetKeywords.size

        return try {

            // ================= AI CALL =================

            val prompt = """
            You are an expert HR Recruiter and ATS optimizer.

            JOB DESCRIPTION:
            $jobDescription

            RESUME TEXT:
            $resumeText

            Give:(use only * or ** for font avoid # or ##)
            1. Match Summary
            -points
            add line
            2. Missing Keywords
            -points
            add line
            3. Things to Avoid
            -points
            add line
            4. Improvement Suggestions
            -points
            add line

            At end:
            AI_MATCH_SCORE: [0-100]
        """.trimIndent()

            val response = generativeModel.generateContent(prompt)

            val aiFeedback =
                response.text ?: "AI returned empty response."

            val parsedAiScore = extractAiScore(aiFeedback)

            val finalCombinedScore =
                if (parsedAiScore > 0) {
                    ((parsedAiScore * 0.8) + (keywordScore * 0.2))
                        .toInt()
                        .coerceIn(0, 100)
                } else {
                    keywordScore
                }

            val summaryMetadata = buildString {
                appendLine("##### Automated Keyword Evaluation")
                appendLine(
                    "**Matched Skills:** ${
                        if (matchedKeywords.isEmpty()) "None"
                        else matchedKeywords.joinToString(", ")
                    }"
                )
                appendLine("**Keyword Match Density:** $keywordScore%")
                appendLine()
                appendLine(aiFeedback)
            }

            val record = AnalysisRecord(
                fileName = fileName,
                rawText = resumeText,
                aiFeedback = summaryMetadata,
                resumeScore = finalCombinedScore,
                timestamp = System.currentTimeMillis()
            )

            analysisDao.insertRecord(record)

            aiFeedback

        } catch (e: Exception) {

            val errorMessage = when {

                e.message?.contains("Unable to resolve host", true) == true ->
                    "No internet connection available."

                e.message?.contains("timeout", true) == true ->
                    "Request timed out. Please try again."

                e.message?.contains("503", true) == true ->
                    "AI service is temporarily unavailable."

                e.message?.contains("429", true) == true ->
                    "Too many requests. Please wait a moment."

                e.message?.contains("API key", true) == true ->
                    "Invalid API configuration."

                else ->
                    "AI analysis failed. Please try again."
            }

            val offlineFeedback = buildString {

                appendLine("SYSTEM_STATUS:")
                appendLine("- $errorMessage")
                appendLine()

                appendLine("MATCH_SUMMARY:")
                appendLine(
                    "- Matched Skills: ${
                        if (matchedKeywords.isEmpty()) "None"
                        else matchedKeywords.joinToString(", ")
                    }"
                )
                appendLine("- Keyword Match Density: $keywordScore%")
                appendLine()

                appendLine("MISSING_KEYWORDS:")
                appendLine("- AI analysis unavailable")
                appendLine()

                appendLine("THINGS_TO_AVOID:")
                appendLine("- Could not generate AI suggestions")
                appendLine()

                appendLine("IMPROVEMENT_SUGGESTIONS:")
                appendLine("- Retry once network/service stabilizes")
                appendLine()

                appendLine("AI_MATCH_SCORE: $keywordScore")
            }

            val record = AnalysisRecord(
                fileName = fileName,
                rawText = resumeText,
                aiFeedback = offlineFeedback,
                resumeScore = keywordScore,
                timestamp = System.currentTimeMillis()
            )

            analysisDao.insertRecord(record)

            offlineFeedback
        }
    }

    /**
     * Resilient keyword scanner extracting core industry domain skills
     */
    private fun extractKeywords(jd: String): List<String> {
        // Broad stop-words list to prevent structural noise from filtering as "skills"
        val stopWords = setOf(
            "about", "above", "across", "after", "against", "along", "around", "at",
            "before", "behind", "below", "beneath", "beside", "between", "beyond",
            "during", "except", "for", "from", "in", "inside", "into", "like",
            "near", "of", "off", "on", "onto", "out", "outside", "over", "past",
            "through", "throughout", "to", "toward", "under", "underneath", "until",
            "up", "upon", "with", "within", "without", "should", "would", "could"
        )

        return jd.split(Regex("[\\s\n,./:!?()|+\\-]+")) // Cleaner token split including paths
            .map { it.trim().lowercase() }
            .filter { token ->
                token.length >= 3 &&
                        token.any { it.isLetter() } &&
                        !stopWords.contains(token)
            }
            .distinct()
    }

    /**
     * Resilient score parser utilizing case-insensitive boundaries and lazy digit lookups
     */
    private fun extractAiScore(text: String): Int {
        val patterns = listOf(
            "AI_MATCH_SCORE:\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE),
            "MATCH_SCORE:\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE),
            "SCORE:\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE)
        )

        for (regex in patterns) {
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 0
            }
        }
        return 0 // Fallback condition handler identifier
    }

    override fun getHistory(): Flow<List<AnalysisRecord>> {
        return analysisDao.getAllRecords()
    }

     override suspend fun deleteRecord(record : AnalysisRecord){
        analysisDao.deleteRecord(record)
    }


}