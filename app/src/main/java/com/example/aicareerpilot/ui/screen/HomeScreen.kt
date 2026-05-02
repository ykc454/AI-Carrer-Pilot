package com.example.aicareerpilot.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.example.aicareerpilot.util.parseBulletPoints


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ResumeViewModel) {

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
                        Color(0xFF606060))
                )
        )
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

                    TitleSection(isTablet)

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
                    latest?.let { HistoryCard(it) }
                }
            }

        } else {
            // 📱 PHONE UI (your original but fixed)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)

            ) {

                Spacer(modifier = Modifier.height(40.dp))

                TitleSection(isTablet)

                Spacer(modifier = Modifier.height(24.dp))

                JDCard(jd) { viewModel.updateJD(it) }

                Spacer(modifier = Modifier.height(20.dp))

                UploadButtonPremium(isLoading) {
                    launcher.launch("application/pdf")
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader()

                Spacer(modifier = Modifier.height(12.dp))

                val latest = history.firstOrNull()
                latest?.let { HistoryCard(it) }
                    ?: Text("No recent activity", color = Color.Gray)


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
fun TitleSection(isTablet: Boolean) {
    Text(
        text = "AI Career Pilot",
        style = if (isTablet)
            MaterialTheme.typography.displayMedium
        else
            MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
    )

    Text(
        text = "Analyze your resume against any job description",
        color = Color.White.copy(alpha = 0.7f)
    )
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

