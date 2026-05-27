package com.example.aicareerpilot.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aicareerpilot.presentation.viewmodel.ResumeViewModel
import com.mikepenz.markdown.m3.Markdown

@Composable
fun HistoryDetailScreen(
    recordId: Int?,
    viewModel: ResumeViewModel
) {
    val history by viewModel.analysisHistory.collectAsState()

    val record = remember(history, recordId) {
        history.find { it.id == recordId }
    }

    if (record == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val scoreColor = when (record.resumeScore) {
        in 0..49 -> colorScheme.error
        in 50..80 -> Color(0xFFFFC107) // Clean amber/yellow
        else -> Color(0xFF4CAF50) // Vibrant green
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- HEADER SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Analysis Results",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.fileName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Circular Score Meter Component
            CircularScoreMeter(
                score = record.resumeScore,
                scoreColor = scoreColor,
                size = 80.dp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- CONTENT SECTION (AI FEEDBACK) ---
        Text(
            text = "AI Career Feedback",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFF444444)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val feedbackText = remember(record.aiFeedback) {
                    record.aiFeedback ?: "*No feedback details available for this record.*"
                }

                Markdown(content = feedbackText)
            }
        }
    }
}

@Composable
fun CircularScoreMeter(
    score: Int,
    scoreColor: Color,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp
) {
    // Animation driver to smoothly roll up the meter when screen loads
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) score / 100f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "ScoreProgress"
    )

    LaunchedEffect(key1 = true) {
        animationTriggered = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Track (Background circle)
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                style = Stroke(width = strokeWidth.toPx())
            )
            // Progress Indicator
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Text Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "match",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}