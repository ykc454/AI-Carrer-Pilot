package com.example.aicareerpilot.ui.screen

import android.R.attr.onClick
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicareerpilot.data.model.AnalysisRecord
import com.example.aicareerpilot.ui.viewmodel.ResumeViewModel
import com.example.aicareerpilot.util.DeviceType
import com.example.aicareerpilot.util.getDeviceType
import com.example.aicareerpilot.util.parseBulletPoints

@Composable
fun HistoryScreen(viewModel: ResumeViewModel) {

    val history by viewModel.analysisHistory.collectAsState()
    val deviceType = getDeviceType()
    val isTablet = deviceType == DeviceType.TABLET

    val backgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF000000),
            Color(0xFF101010),
            Color(0xFF505050)
        )
    )
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No history yet",
                color = Color.Gray,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }else{
        if (isTablet) {
            // 🔥 TABLET → GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf( Color.White.copy(alpha = 0.08f),
                                    Color.Transparent )
                            ),
                            radius = size.width * 0.8f,
                            center = Offset(size.width * 0.3f,
                                size.height * 0.2f
                            )
                        )
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { record ->
                    HistoryCard(record)
                }
            }
        } else {
            // 📱 PHONE → LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf( Color.White.copy(alpha = 0.08f),
                                    Color.Transparent )
                            ),
                            radius = size.width * 0.8f,
                            center = Offset(size.width * 0.3f,
                                size.height * 0.2f
                            )
                        )
                    }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { record ->
                    HistoryCard(record)
                }
            }
        }
    }

}

@Composable
fun HistoryCard(record: AnalysisRecord) {

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(
                if (isExpanded)
                    Modifier.heightIn(min = 500.dp, max = 500.dp) // expanded size
                else
                    Modifier.height(150.dp) // collapsed fixed size
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        onClick = { isExpanded = !isExpanded }
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {

            // FILE NAME
            Text(
                text = record.fileName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // SCORE (make it more prominent)
            val scoreColor = when (record.resumeScore) {
                in 0..49 -> MaterialTheme.colorScheme.error
                in 50..80 -> Color(0xFFFFC107) // amber (better than pure yellow)
                else -> Color(0xFF4CAF50) // green
            }

            Text(
                text = "Score: ${record.resumeScore}%",
                style = MaterialTheme.typography.bodyMedium,
                color = scoreColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥 COLLAPSED PREVIEW (very important)
            if (!isExpanded) {
                Text(
                    text = record.aiFeedback ?: "No feedback available",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 🔥 EXPANDED FULL CONTENT
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = record.aiFeedback ?: "No feedback available",
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Show less" else "Show more",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )

                if (!isExpanded) {
                    val formattedTime = remember(record.timestamp) {
                        java.text.SimpleDateFormat(
                            "dd MMM, hh:mm a",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(record.timestamp))
                    }

                    Text(
                        text = formattedTime,
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}