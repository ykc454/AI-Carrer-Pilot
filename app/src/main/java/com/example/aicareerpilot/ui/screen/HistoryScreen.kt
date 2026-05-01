package com.example.aicareerpilot.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicareerpilot.data.model.AnalysisRecord
import com.example.aicareerpilot.ui.viewmodel.ResumeViewModel
import com.example.aicareerpilot.util.parseBulletPoints

@Composable
fun HistoryScreen(viewModel: ResumeViewModel) {

    val history by viewModel.analysisHistory.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF000000),
                    Color(0xFF2F2B2B),
                    Color(0xFF4F4D4D)
                )
            )
        )
            .padding(16.dp)
    ) {
        items(history) { record ->
            HistoryCard(record)
        }
    }
}

@Composable
fun HistoryCard(record: AnalysisRecord,) {

    var isExpanded by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        elevation = CardDefaults.cardElevation(6.dp), // subtle depth
        shape = RoundedCornerShape(20.dp),
        onClick = { isExpanded = !isExpanded }
    ){

        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = record.fileName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Badge(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        "${record.resumeScore}%",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val points = parseBulletPoints(record.aiFeedback)

            Column {
                points.take(if (isExpanded) points.size else 3).forEach {
                    Text(
                        text = "• $it",
                        color = Color.White.copy(alpha = 0.75f),
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            if (!isExpanded) {
                Text(
                    text = "Tap to expand",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}