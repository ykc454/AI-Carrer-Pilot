package com.nextgendevs.aicareerpilot.domain.model

/**
 * Structured view of the AI feedback text saved with each analysis.
 *
 * Gemini answers in a fixed plain-text format (MATCH_SUMMARY:, MATCHED_SKILLS:, ...),
 * and the repository prepends a keyword header. [AnalysisReportParser] turns that
 * text into this model so the UI can show real sections, chips and lists instead
 * of one block of text. Pure Kotlin, no Android types, so it is unit-testable.
 */
data class AnalysisReport(
    val aiScore: Int? = null,
    val keywordDensity: Int? = null,
    val keywordMatches: List<String> = emptyList(),
    val summary: List<String> = emptyList(),
    val matchedSkills: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val atsTips: List<String> = emptyList(),
    val verdict: String? = null,
    /** Set when the AI call failed and an offline keyword-only report was saved. */
    val systemStatus: List<String> = emptyList()
) {
    /** True when no known section was found (very old or unexpected text). */
    val isEmpty: Boolean
        get() = summary.isEmpty() && matchedSkills.isEmpty() && missingKeywords.isEmpty() &&
                improvements.isEmpty() && atsTips.isEmpty() && verdict == null &&
                systemStatus.isEmpty()
}

object AnalysisReportParser {

    private enum class Section {
        SUMMARY, MATCHED, MISSING, IMPROVE, TIPS, VERDICT, STATUS, IGNORED
    }

    private val sectionKeys = mapOf(
        "MATCH_SUMMARY" to Section.SUMMARY,
        "MATCHED_SKILLS" to Section.MATCHED,
        "MISSING_KEYWORDS" to Section.MISSING,
        "THINGS_TO_IMPROVE" to Section.IMPROVE,
        "IMPROVEMENT_SUGGESTIONS" to Section.IMPROVE,
        "THINGS_TO_AVOID" to Section.IMPROVE,
        "ATS_OPTIMIZATION_TIPS" to Section.TIPS,
        "FINAL_VERDICT" to Section.VERDICT,
        "SYSTEM_STATUS" to Section.STATUS
    )

    // e.g. "MATCH_SUMMARY:" or "MATCH_SCORE: 43" (after markdown is stripped)
    private val headerRegex = Regex("^([A-Z][A-Z_ ]{3,}):\\s*(.*)$")
    private val bulletPrefix = Regex("^(?:[-*•–]|\\d+[.)])\\s*")
    private val number = Regex("(\\d{1,3})")

    fun parse(text: String?): AnalysisReport {
        if (text.isNullOrBlank()) return AnalysisReport()

        var aiScore: Int? = null
        var density: Int? = null
        var keywordMatches = emptyList<String>()
        val lists = Section.entries.associateWith { mutableListOf<String>() }
        var current = Section.IGNORED

        for (raw in text.lines()) {
            val line = clean(raw)
            if (line.isEmpty()) continue

            // Header added by the repository (keyword engine)
            when {
                line.startsWith("Matched Skills:", ignoreCase = true) && current == Section.IGNORED -> {
                    keywordMatches = splitList(line.substringAfter(':'))
                    continue
                }
                line.startsWith("Keyword Match Density:", ignoreCase = true) -> {
                    density = number.find(line)?.value?.toIntOrNull()
                    continue
                }
                line.equals("Automated Keyword Evaluation", ignoreCase = true) -> continue
            }

            val header = headerRegex.matchEntire(line)
            if (header != null) {
                val key = header.groupValues[1].trim().replace(' ', '_')
                val rest = header.groupValues[2].trim()

                if (key == "MATCH_SCORE" || key == "AI_MATCH_SCORE") {
                    aiScore = number.find(rest)?.value?.toIntOrNull()?.coerceIn(0, 100)
                    current = Section.IGNORED
                    continue
                }
                val section = sectionKeys[key]
                if (section != null) {
                    current = section
                    if (rest.isNotEmpty()) lists.getValue(section) += stripBullet(rest)
                    continue
                }
            }

            if (current != Section.IGNORED) {
                val item = stripBullet(line)
                if (item.isNotEmpty()) lists.getValue(current) += item
            }
        }

        val verdict = lists.getValue(Section.VERDICT).joinToString(" ").ifBlank { null }

        return AnalysisReport(
            aiScore = aiScore,
            keywordDensity = density,
            keywordMatches = keywordMatches,
            summary = lists.getValue(Section.SUMMARY),
            matchedSkills = lists.getValue(Section.MATCHED).flatMap(::splitIfInline),
            missingKeywords = lists.getValue(Section.MISSING).flatMap(::splitIfInline)
                .filterNot { isNoneItem(it) },
            improvements = lists.getValue(Section.IMPROVE),
            atsTips = lists.getValue(Section.TIPS),
            verdict = verdict,
            systemStatus = lists.getValue(Section.STATUS)
        )
    }

    /** "None", "None directly missing ...", "N/A" mean the list is really empty. */
    private fun isNoneItem(item: String): Boolean {
        val t = item.lowercase()
        return t == "n/a" || t.startsWith("none") || t.startsWith("no missing")
    }

    /** Removes markdown decoration: #, **, __, backticks. */
    private fun clean(raw: String): String =
        raw.trim()
            .trimStart('#')
            .replace("**", "")
            .replace("__", "")
            .replace("`", "")
            .trim()

    private fun stripBullet(s: String): String = s.replace(bulletPrefix, "").trim()

    private fun splitList(s: String): List<String> =
        s.split(',').map { it.trim() }.filter { it.isNotEmpty() && !it.equals("None", true) }

    /** Skills sometimes arrive as "Kotlin, Compose, Room" on one line. */
    private fun splitIfInline(item: String): List<String> =
        if (item.contains(',') && item.length < 120) splitList(item) else listOf(item)
}
