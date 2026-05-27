package com.example.aicareerpilot.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.example.aicareerpilot.presentation.viewmodel.ResumeViewModel
import com.example.aicareerpilot.util.DeviceType
import com.example.aicareerpilot.util.getDeviceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: ResumeViewModel,
    onRecordClick: (AnalysisRecord) -> Unit // Added callback for navigation
) {
    val history by viewModel.analysisHistory.collectAsState()
    val deviceType = getDeviceType()
    val isTablet = deviceType == DeviceType.TABLET

    val backgroundBrush = Brush.verticalGradient(
        listOf(
            Color(0xFF000000),
            Color(0xFF000000),
            Color(0xFF111111)
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
    } else {
        if (isTablet) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                            ),
                            radius = size.width * 0.8f,
                            center = Offset(size.width * 0.3f, size.height * 0.2f)
                        )
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { record ->
                    HistoryCard(record = record, onClick = { onRecordClick(record) }, viewModel = viewModel)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                            ),
                            radius = size.width * 0.8f,
                            center = Offset(size.width * 0.3f, size.height * 0.2f)
                        )
                    }
                    .padding(start = 10.dp,end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { record ->
                    HistoryCard(record = record, onClick = { onRecordClick(record) }, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    viewModel: ResumeViewModel,
    record: AnalysisRecord,
    onClick: () -> Unit // Replaced internal state with a click parameter
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scoreColor = when (record.resumeScore) {
        in 0..49 -> Color(0xFFFF5A5F)
        in 50..80 -> Color(0xFFFFB547)
        else -> Color(0xFF32D583)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Reduced padding slightly for clean list look
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        border = BorderStroke(
            1.dp,
            colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorScheme.surface.copy(alpha = 1f),
                            colorScheme.surfaceVariant.copy(alpha = 0.01f)
                        )
                    )
                )
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                // 🔹 HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = "Resume Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // 🔹 Score Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF111111))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(scoreColor)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${record.resumeScore}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 🔹 FOOTER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val formattedTime = remember(record.timestamp) {
                        SimpleDateFormat(
                            "dd MMM, hh:mm a",
                            Locale.getDefault()
                        ).format(Date(record.timestamp))
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    IconButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "View details",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    if (showDeleteDialog) {
        DeleteDialog(
            onConfirm = {
                viewModel.deleteRecord(record)
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}
@Composable
fun DeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Delete Record")
        },

        text = {
            Text("Are you sure you want to delete this analysis record?")
        },

        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    "Yes",
                    color = Color.Red
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },

        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )
}
