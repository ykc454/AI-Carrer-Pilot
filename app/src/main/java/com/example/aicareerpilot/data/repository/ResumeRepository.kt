package com.example.aicareerpilot.data.repository

import com.example.aicareerpilot.data.local.AnalysisDao
import com.example.aicareerpilot.data.model.AnalysisRecord
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResumeRepository @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val analysisDao: AnalysisDao
) {
    suspend fun analyzeWithJD(fileName: String, resumeText: String, jobDescription: String): String {
        // 1. Manual Keyword Matching (Logic Score)
        val keywords = extractKeywords(jobDescription)
        val foundKeywords = keywords.filter { resumeText.contains(it, ignoreCase = true) }
        val keywordScore = if (keywords.isEmpty()) 0 else (foundKeywords.size * 100) / keywords.size

        // 2. AI Analysis (Context Score)
        val prompt = """
            Don't use any text formatting 
            You are an expert HR Recruiter. 
            JOB DESCRIPTION:
            $jobDescription
            
            RESUME TEXT:
            $resumeText
            
            Based on the Job Description above, please provide:
            1. Match Summary: How well does the candidate fit?
            2. Missing Keywords: What specific skills from the JD are missing in the resume?
            3. Things to Avoid: Point out any red flags or unnecessary info in the resume.
            4. Improvement Suggestions: How to better align this resume with THIS specific JD.
            5. AI_MATCH_SCORE: Provide a score from 0-100 based on experience and context.
            
            IMPORTANT: End your response with: AI_MATCH_SCORE: [number]
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val aiFeedback = response.text ?: "No analysis generated"

            // 3. Combine Scores
            val aiScore = extractAiScore(aiFeedback)
            val finalCombinedScore = aiScore

            // 4. Save to Room
            val record = AnalysisRecord(
                fileName = fileName,
                rawText = resumeText,
                aiFeedback = "Keyword Match: ${foundKeywords.joinToString()}\n\n$aiFeedback",
                resumeScore = finalCombinedScore
            )
            analysisDao.insertRecord(record)

            aiFeedback
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    // Simple keyword extractor (picks words longer than 5 letters as "skills")
    private fun extractKeywords(jd: String): List<String> {
        return jd.split(" ", "\n", ",")
            .map { it.trim().lowercase().replace(Regex("[^a-zA-Z]"), "") }
            .filter { it.length > 5 } // Basic filter for technical words/skills
            .distinct()
    }

    private fun extractAiScore(text: String): Int {
        val regex = "AI_MATCH_SCORE:\\s*(\\d+)".toRegex()
        return regex.find(text)?.groupValues?.get(1)?.toInt() ?: 0
    }

    fun getHistory(): Flow<List<AnalysisRecord>> {
        return analysisDao.getAllRecords()
    }
}