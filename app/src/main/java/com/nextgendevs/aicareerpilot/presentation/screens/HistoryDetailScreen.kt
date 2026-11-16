package com.nextgendevs.aicareerpilot.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.nextgendevs.aicareerpilot.domain.model.AnalysisReport
import com.nextgendevs.aicareerpilot.domain.model.AnalysisReportParser
import com.nextgendevs.aicareerpilot.presentation.theme.AppColors as C
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeViewModel

@Composable
fun HistoryDetailScreen(
    recordId: Int?,
    resumeViewModel: ResumeViewModel
) {
    val history by resumeViewModel.analysisHistory.collectAsState()
    val record = remember(history, recordId) { history.find { it.id == recordId } }

    if (record == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(C.Background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = C.Accent, strokeWidth = 2.dp) }
        return
    }

    val report = remember(record.aiFeedback) { AnalysisReportParser.parse(record.aiFeedback) }
    val date = remember(record.timestamp) {
        java.text.SimpleDateFormat("dd MMM yyyy · hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date(record.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroCard(
            fileName = record.fileName,
            date = date,
            score = record.resumeScore,
            aiScore = report.aiScore ?: record.aiMatchScore,
            keywordDensity = report.keywordDensity
        )

        if (report.systemStatus.isNotEmpty()) {
            StatusBanner(report.systemStatus.joinToString(" "))
        }

        report.verdict?.let { VerdictCard(it) }

        if (report.isEmpty) {
            // Very old / unexpected format: fall back to plain markdown
            SectionLabel("AI analysis")
            PremiumCard { Markdown(content = record.aiFeedback) }
        } else {
            ReportSections(report)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------- sections

@Composable
private fun ReportSections(report: AnalysisReport) {
    if (report.summary.isNotEmpty()) {
        SectionLabel("Summary")
        PremiumCard {
            report.summary.forEachIndexed { i, point ->
                BulletRow(point)
                if (i != report.summary.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }

    val matched = report.matchedSkills.ifEmpty { report.keywordMatches }
    if (matched.isNotEmpty() || report.missingKeywords.isNotEmpty()) {
        SectionLabel("Skills")
        PremiumCard {
            if (matched.isNotEmpty()) {
                ChipGroup("Matched", "${matched.size}", matched, C.Good)
            }
            if (matched.isNotEmpty() && report.missingKeywords.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 18.dp), color = C.Hairline)
            }
            if (report.missingKeywords.isNotEmpty()) {
                ChipGroup(
                    "Missing", "${report.missingKeywords.size}",
                    report.missingKeywords, C.Bad
                )
            }
        }
    }

    if (report.improvements.isNotEmpty()) {
        SectionLabel("What to improve")
        PremiumCard(padding = 0.dp) {
            report.improvements.forEachIndexed { i, item ->
                NumberedRow(i + 1, item)
                if (i != report.improvements.lastIndex) {
                    HorizontalDivider(Modifier.padding(start = 64.dp), color = C.Hairline)
                }
            }
        }
    }

    if (report.atsTips.isNotEmpty()) {
        SectionLabel("ATS optimisation tips")
        PremiumCard(padding = 0.dp) {
            report.atsTips.forEachIndexed { i, tip ->
                IconRow(Icons.Outlined.Lightbulb, tip)
                if (i != report.atsTips.lastIndex) {
                    HorizontalDivider(Modifier.padding(start = 64.dp), color = C.Hairline)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- hero

@Composable
private fun HeroCard(
    fileName: String,
    date: String,
    score: Int,
    aiScore: Int,
    keywordDensity: Int?
) {
    val tone = scoreTone(score)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = C.Background,
        border = BorderStroke(1.dp, cardBorder()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        // dark: graphite lift · light: faint lavender wash on white
                        if (C.IsDark) listOf(C.Surface, C.Background)
                        else listOf(C.AccentSoft, C.Surface)
                    )
                )
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "RESUME REPORT",
                        color = C.Accent,
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        fileName,
                        color = C.TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(date, color = C.TextMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(14.dp))
                    StatusPill(text = tone.label, color = tone.color)
                }
                Spacer(Modifier.width(12.dp))
                CircularScoreMeter(score = score, scoreColor = tone.color, size = 112.dp)
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(Icons.Outlined.AutoAwesome, "AI match", "$aiScore%", aiScore, Modifier.weight(1f))
                StatTile(
                    Icons.Outlined.Insights, "Keywords",
                    keywordDensity?.let { "$it%" } ?: "—",
                    keywordDensity ?: 0, Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, label: String, value: String, progress: Int, modifier: Modifier) {
    val tone = scoreTone(progress)
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (C.IsDark) C.SurfaceHigh else C.Background)
            .border(1.dp, C.TextPrimary.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = C.TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = C.TextSecondary, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text(value, color = C.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        val animated by animateFloatAsState(progress / 100f, tween(900), label = "tile")
        Box(
            Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(C.Hairline)
        ) {
            Box(
                Modifier.fillMaxWidth(animated.coerceIn(0f, 1f)).fillMaxHeight()
                    .clip(CircleShape).background(tone.color)
            )
        }
    }
}

// ---------------------------------------------------------------- cards & rows

@Composable
private fun VerdictCard(verdict: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (C.IsDark) C.Background else C.AccentSoft,
        border = BorderStroke(1.dp, if (C.IsDark) cardBorder() else C.Accent.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .then(if (C.IsDark) Modifier.background(cardBrush()) else Modifier)
                .padding(20.dp)
        ) {
            Icon(Icons.Outlined.FormatQuote, null, tint = C.Accent, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Final verdict", color = C.Accent,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    verdict, color = C.TextPrimary,
                    style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(C.WarnSoft)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CloudOff, null, tint = C.Warn, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            "Keyword-only report. $message",
            color = C.TextPrimary, style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        color = C.TextMuted,
        fontSize = 12.sp,
        letterSpacing = 1.6.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 10.dp)
    )
}

/** Dark: subtle near-black gradient like the Home cards. Light: plain white. */
@Composable
private fun cardBrush(): Brush =
    if (C.IsDark) Brush.verticalGradient(listOf(C.Surface, C.Background))
    else Brush.verticalGradient(listOf(C.Surface, C.Surface))

/** Border used by the Home cards: 8 % of the text colour. */
@Composable
private fun cardBorder(): Color = if (C.IsDark) C.TextPrimary.copy(alpha = 0.08f) else C.Hairline

@Composable
private fun PremiumCard(padding: Dp = 20.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(C.Background)
            .background(cardBrush())
            .border(1.dp, cardBorder(), RoundedCornerShape(24.dp))
            .padding(padding),
        content = content
    )
}

@Composable
private fun BulletRow(text: String) {
    Row {
        Box(
            Modifier.padding(top = 8.dp).size(6.dp).clip(CircleShape).background(C.Accent)
        )
        Spacer(Modifier.width(14.dp))
        Text(text, color = C.TextPrimary, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
    }
}

@Composable
private fun NumberedRow(index: Int, text: String) {
    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(C.AccentSoft),
            contentAlignment = Alignment.Center
        ) {
            Text("$index", color = C.Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text, color = C.TextPrimary, style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun IconRow(icon: ImageVector, text: String) {
    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(C.WarnSoft),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = C.Warn, modifier = Modifier.size(17.dp)) }
        Spacer(Modifier.width(16.dp))
        Text(
            text, color = C.TextPrimary, style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(title: String, count: String, items: List<String>, dot: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(10.dp))
        Text(title, color = C.TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Text(count, color = C.TextMuted, style = MaterialTheme.typography.labelMedium)
    }
    Spacer(Modifier.height(14.dp))

    // "Kotlin (listed as language)" -> "Kotlin"; long sentences go below as notes
    val labels = items.map { it.substringBefore(" (").trim() }.filter { it.isNotEmpty() }
    val (short, long) = labels.zip(items).partition { (label, _) -> label.length <= 32 }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        short.forEach { (label, _) -> Pill(label, dot) }
    }
    long.forEach { (_, full) ->
        Spacer(Modifier.height(10.dp))
        Row {
            Text("–", color = C.TextMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(8.dp))
            Text(full, color = C.TextSecondary, style = MaterialTheme.typography.bodySmall, lineHeight = 19.sp)
        }
    }
}

/** Neutral chip: soft fill, hairline border, small status dot. */
@Composable
private fun Pill(text: String, dot: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(C.TextPrimary.copy(alpha = if (C.IsDark) 0.05f else 0.03f))
            .border(1.dp, C.Hairline, RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp)
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(dot.copy(alpha = 0.85f)))
        Spacer(Modifier.width(7.dp))
        Text(
            text,
            color = C.TextPrimary.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Status label (e.g. "Needs work") with a dot, no loud fill. */
@Composable
private fun StatusPill(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, C.Hairline, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(text, color = C.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ---------------------------------------------------------------- score

private data class Tone(val color: Color, val soft: Color, val label: String)

@Composable
private fun scoreTone(score: Int) = when {
    score >= 75 -> Tone(C.Good, C.GoodSoft, "Strong match")
    score >= 50 -> Tone(C.Warn, C.WarnSoft, "Fair match")
    else -> Tone(C.Bad, C.BadSoft, "Needs work")
}

@Composable
fun CircularScoreMeter(
    score: Int,
    scoreColor: Color,
    size: Dp = 80.dp,
    strokeWidth: Dp = 9.dp
) {
    var started by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (started) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1100),
        label = "ScoreProgress"
    )
    LaunchedEffect(Unit) { started = true }

    val track = C.Hairline
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            drawCircle(color = track, style = Stroke(width = strokeWidth.toPx()))
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", color = C.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            Text("ATS SCORE", color = C.TextMuted, fontSize = 9.sp, letterSpacing = 1.2.sp)
        }
    }
}
