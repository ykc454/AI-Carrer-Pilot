package com.example.aicareerpilot.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aicareerpilot.data.model.AnalysisRecord
import com.example.aicareerpilot.ui.viewmodel.ResumeViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.example.aicareerpilot.util.DeviceType
import com.example.aicareerpilot.util.getDeviceType
import com.mikepenz.markdown.m3.Markdown


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ResumeViewModel,
    onRecordClick: (AnalysisRecord) -> Unit
) {

    val isLoading by viewModel.isLoading.collectAsState()
    val history by viewModel.analysisHistory.collectAsState()
    val jd by viewModel.jobDescription.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processResume(it, "Resume_${System.currentTimeMillis()}.pdf") }
    }

    BoxWithConstraints( modifier = Modifier
        .fillMaxSize()
        .background(
            brush =
                Brush.verticalGradient(
                    listOf( Color(0xFF000000),
                        Color(0xFF0A0A0A),
                        Color(0xFF484747)
                    )
                )
        )
        .drawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf( Color.White.copy(alpha = 0.08f),
                        Color.Transparent )
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.3f, size.height * 0.05f)
            )
        }
    ) {

        val isTablet = maxWidth > 600.dp

        if (isTablet) {
            // 🔥 TABLET UI
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {

                // LEFT SIDE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp)
                ) {



                    Spacer(Modifier.height(24.dp))

                    JDCard(jd) { viewModel.updateJD(it) }
                }

                // RIGHT SIDE
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)
                ) {

                    UploadButtonPremium(isLoading) {
                        launcher.launch("application/pdf")
                    }

                    Spacer(Modifier.height(24.dp))

                    val latest = history.firstOrNull()
                    latest?.let { record ->
                        HistoryCard(
                            record = record,
                            onClick = {
                                onRecordClick(record)
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }

        } else {
            // 📱 PHONE UI (your original but fixed)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 80.dp)
            ) {
                item { Spacer(modifier = Modifier.height(20.dp)) }

                item { JDCard(jd) { viewModel.updateJD(it) } }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    UploadButtonPremium(isLoading) {
                        launcher.launch("application/pdf")
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item { SectionHeader() }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {

                    val latest = history.firstOrNull()

                    latest?.let { record ->

                        HistoryCard(
                            record = record,
                            onClick = {
                                onRecordClick(record)
                            },
                            viewModel = viewModel
                        )

                    } ?: Text(
                        text = "No recent activity",
                        color = Color.Gray
                    )
                }
            }
            val year = "2026"
            Text(
                text = "© $year Yash Chaudhari — All rights reserved",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun TitleSection() {
    Text(
        text = "AI Career Pilot",
        style = if (getDeviceType() == DeviceType.TABLET)
            MaterialTheme.typography.displayMedium
        else
            MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
    )
        Spacer(Modifier.height(50.dp))
    Text(
        text = "Analyze your resume against any job description",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.7f)
    )
}
@Composable
fun HomeCard(record: AnalysisRecord) {

    var isExpanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val scoreColor = when (record.resumeScore) {
        in 0..49 -> colorScheme.error
        in 50..80 -> Color.Yellow
        else -> Color.Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.6f) // glass base
        ),
        border = BorderStroke(
            1.dp,
            colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        onClick = { isExpanded = !isExpanded }
    ) {

        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorScheme.surface.copy(alpha = 0.7f),
                            colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(20.dp)
            ) {

                // 🔹 HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            text = record.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = "Resume Analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // 🔹 Score badge (clean)
                    Box(
                        modifier = Modifier
                            .background(
                                scoreColor.copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                scoreColor.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${record.resumeScore}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = scoreColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))


                // 🔹 EXPANDED CONTENT
                if (isExpanded) {

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(
                            1.dp,
                            colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Markdown(
                                content = record.aiFeedback ?: "No feedback available"
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val date = remember(record.timestamp) {
                        java.text.SimpleDateFormat(
                            "dd MMM yyyy",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(record.timestamp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "Show less",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val formattedTime = remember(record.timestamp) {
                            java.text.SimpleDateFormat(
                                "dd MMM, hh:mm a",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date(record.timestamp))
                        }

                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Show more",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JDCard(jd: String, onChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A).copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = jd,
            onValueChange = onChange,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            placeholder = {
                Text("Paste Job Description...", color = Color.LightGray)
            },
            // This sets the icon on the keyboard
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            // This defines the behavior when the "Done" icon is clicked
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    // You can also add logic here to trigger the "Match Score" calculation
                }
            )
        )
    }
}

@Composable
fun UploadButtonPremium(isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.White, Color(0xFFE0E0E0))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.Black)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileUpload, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Analyze Match Score", color = Color.Black)
            }
        }
    }
}

@Composable
fun SectionHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, null, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(
            "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

