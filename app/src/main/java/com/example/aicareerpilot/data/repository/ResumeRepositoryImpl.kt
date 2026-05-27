package com.example.aicareerpilot.data.repository

import com.example.aicareerpilot.data.local.AnalysisDao
import com.example.aicareerpilot.data.model.gemini_response.AnalysisRecord
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
        val safeResumeText =
            resumeText.take(12000)

        val safeJobDescription =
            jobDescription.take(4000)
        val matchedKeywords = targetKeywords.filter {
            resumeText.contains(it, ignoreCase = true)
        }

        val keywordScore =
            if (targetKeywords.isEmpty()) 0
            else (matchedKeywords.size * 100) / targetKeywords.size

        return try {

            // ================= AI CALL =================

            val prompt = """
You are a senior ATS recruiter, hiring manager, and resume optimization expert.

Your task is to analyze the RESUME against the JOB DESCRIPTION and provide a strict structured response.

IMPORTANT RULES:
- Return ONLY plain text.
- Do NOT use markdown headings like # or ##.
- Do NOT add explanations outside the required format.
- Be accurate and professional.
- Evaluate ATS compatibility, technical skills, role alignment, experience relevance, and missing keywords.
- Score must be realistic and strict.
- If resume lacks major required skills, reduce score accordingly.
- Mention both strengths and weaknesses.

OUTPUT FORMAT (FOLLOW EXACTLY):

MATCH_SCORE: <number between 0-100>

MATCH_SUMMARY:
- point
- point
- point

MATCHED_SKILLS:
- skill
- skill
- skill

MISSING_KEYWORDS:
- keyword
- keyword
- keyword

THINGS_TO_IMPROVE:
- point
- point
- point

ATS_OPTIMIZATION_TIPS:
- tip
- tip
- tip

FINAL_VERDICT:
- Short final evaluation about candidate suitability.

JOB DESCRIPTION:
$jobDescription

RESUME:
$resumeText

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

        val techSkills = listOf(

            // =========================
            // ANDROID
            // =========================
            "android",
            "kotlin",
            "java",
            "jetpack compose",
            "compose",
            "xml",
            "mvvm",
            "mvi",
            "clean architecture",
            "coroutines",
            "flow",
            "livedata",
            "room",
            "retrofit",
            "dagger",
            "hilt",
            "navigation component",
            "paging 3",
            "workmanager",
            "firebase",
            "firebase auth",
            "firebase firestore",
            "firebase messaging",
            "firebase analytics",
            "material design",
            "material 3",
            "exoplayer",
            "coil",
            "glide",
            "ktor",
            "sqlite",

            // =========================
            // IOS
            // =========================
            "swift",
            "swiftui",
            "uikit",
            "objective c",
            "core data",
            "combine",
            "xcode",
            "cocoapods",

            // =========================
            // FRONTEND
            // =========================
            "html",
            "css",
            "scss",
            "sass",
            "tailwind",
            "bootstrap",
            "javascript",
            "typescript",
            "react",
            "nextjs",
            "vue",
            "nuxt",
            "angular",
            "redux",
            "zustand",
            "webpack",
            "vite",
            "jquery",

            // =========================
            // BACKEND
            // =========================
            "node",
            "nodejs",
            "express",
            "nestjs",
            "spring",
            "spring boot",
            "django",
            "flask",
            "fastapi",
            "laravel",
            "php",
            "ruby on rails",
            "golang",
            "gin",
            "ktor",
            "rest api",
            "graphql",
            "microservices",

            // =========================
            // DATABASE
            // =========================
            "sql",
            "mysql",
            "postgresql",
            "sqlite",
            "mongodb",
            "redis",
            "firebase",
            "firestore",
            "oracle",
            "supabase",
            "dynamodb",
            "cassandra",

            // =========================
            // CLOUD / DEVOPS
            // =========================
            "aws",
            "azure",
            "gcp",
            "google cloud",
            "docker",
            "kubernetes",
            "jenkins",
            "github actions",
            "gitlab ci",
            "terraform",
            "ansible",
            "nginx",
            "linux",
            "bash",
            "git",
            "github",
            "ci/cd",

            // =========================
            // AI / ML / DATA SCIENCE
            // =========================
            "python",
            "tensorflow",
            "pytorch",
            "keras",
            "machine learning",
            "deep learning",
            "nlp",
            "computer vision",
            "opencv",
            "pandas",
            "numpy",
            "scikit learn",
            "data science",
            "data analysis",
            "power bi",
            "tableau",
            "hugging face",
            "langchain",
            "openai",
            "gemini",
            "llm",
            "rag",
            "vector database",
            "prompt engineering",

            // =========================
            // CYBERSECURITY
            // =========================
            "penetration testing",
            "ethical hacking",
            "burp suite",
            "wireshark",
            "metasploit",
            "owasp",
            "network security",
            "cryptography",
            "soc",
            "siem",

            // =========================
            // TESTING / QA
            // =========================
            "junit",
            "espresso",
            "mockk",
            "mockito",
            "selenium",
            "cypress",
            "playwright",
            "postman",
            "unit testing",
            "ui testing",
            "automation testing",

            // =========================
            // UI/UX DESIGN
            // =========================
            "figma",
            "adobe xd",
            "photoshop",
            "illustrator",
            "ui design",
            "ux design",
            "wireframing",
            "prototyping",

            // =========================
            // PROJECT MANAGEMENT
            // =========================
            "agile",
            "scrum",
            "jira",
            "confluence",
            "kanban",
            "product management",

            // =========================
            // BUSINESS / ANALYTICS
            // =========================
            "excel",
            "powerpoint",
            "communication",
            "leadership",
            "problem solving",
            "critical thinking",
            "business analysis",
            "stakeholder management",

            // =========================
            // MARKETING
            // =========================
            "seo",
            "sem",
            "google ads",
            "meta ads",
            "content marketing",
            "social media marketing",
            "email marketing",
            "copywriting",
            "analytics",

            // =========================
            // FINANCE
            // =========================
            "financial analysis",
            "accounting",
            "quickbooks",
            "taxation",
            "investment banking",
            "forecasting",

            // =========================
            // HR / RECRUITMENT
            // =========================
            "recruitment",
            "talent acquisition",
            "employee engagement",
            "payroll",
            "hr analytics",

            // =========================
            // SALES
            // =========================
            "salesforce",
            "crm",
            "b2b sales",
            "lead generation",
            "negotiation",

            // =========================
            // HEALTHCARE
            // =========================
            "patient care",
            "clinical research",
            "medical coding",
            "ehr",
            "hipaa",

            // =========================
            // EDUCATION
            // =========================
            "curriculum development",
            "classroom management",
            "teaching",
            "lesson planning",

            // =========================
            // GENERAL SOFTWARE TERMS
            // =========================
            "api",
            "sdk",
            "oop",
            "data structures",
            "algorithms",
            "system design",
            "multithreading",
            "networking",
            "authentication",
            "authorization",
            "oauth",
            "jwt",
            "performance optimization"
        )

        val normalizedJD = jd.lowercase()

        return techSkills.filter { skill ->
            normalizedJD.contains(skill)
        }.distinct()
    }

    /**
     * Resilient score parser utilizing case-insensitive boundaries and lazy digit lookups
     */
    private fun extractAiScore(text: String): Int {

        val regex = Regex(
            "MATCH_SCORE:\\s*(\\d+)",
            RegexOption.IGNORE_CASE
        )

        return regex.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?.coerceIn(0, 100)
            ?: 0
    }

    override fun getHistory(): Flow<List<AnalysisRecord>> {
        return analysisDao.getAllRecords()
    }

     override suspend fun deleteRecord(record : AnalysisRecord){
        analysisDao.deleteRecord(record)
    }


}