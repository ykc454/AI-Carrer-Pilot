package com.example.aicareerpilot.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aicareerpilot.domain.model.AnalysisRecord
import com.example.aicareerpilot.presentation.viewmodel.ResumeUiState
import com.example.aicareerpilot.presentation.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ResumeViewModel,
    onRecordClick: (AnalysisRecord) -> Unit
) {
    val context = LocalContext.current

    // 1. Observe your true single source of truth from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.analysisHistory.collectAsState()
    val jd by viewModel.jobDescription.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    // Derived state to determine if we should show loading spinning wheels
    val isLoading = uiState is ResumeUiState.Loading

    // 2. Handle side-effects for Success and Error states cleanly
    LaunchedEffect(uiState) {
        when (uiState) {
            is ResumeUiState.Success -> {
                Toast.makeText(context, "Analysis Complete!", Toast.LENGTH_SHORT).show()

                // If you want to automatically open the newest record on success:
                history.firstOrNull()?.let { newestRecord ->
                    onRecordClick(newestRecord)
                }

                // Reset back to Idle so the user can upload another one later
                viewModel.resetUiState()
            }
            is ResumeUiState.Error -> {
                val errorMessage = (uiState as ResumeUiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.processResume(it, "Resume_${System.currentTimeMillis()}.pdf") }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF000000),
                        Color(0xFF0A0A0A),
                        Color(0xFF484747)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
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
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                ) {
                    UploadButtonPremium(isLoading) {
                        launcher.launch("application/pdf")
                    }

                    Spacer(Modifier.height(24.dp))

                    val latest = history.firstOrNull()
                    latest?.let { record ->
                        HistoryCard(
                            record = record,
                            onClick = { onRecordClick(record) },
                            viewModel = viewModel
                        )
                    }
                }
            }
        } else {
            // 📱 PHONE UI
            Box(modifier = Modifier.fillMaxSize()) {
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
                    item {
                        SectionHeader(
                            onHelpClick = {
                                showHelpDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }

                    item {
                        val latest = history.firstOrNull()
                        if (latest != null) {
                            HistoryCard(
                                record = latest,
                                onClick = { onRecordClick(latest) },
                                viewModel = viewModel
                            )
                        } else {
                            Text(
                                text = "No recent activity",
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
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

        // 3. Optional: Block UI interactions with a clean overlay during heavy background analysis
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {}, // absorbs clicks
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing Resume...", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
    if (showHelpDialog) {
        helpDialog(
            onDismiss = {
                showHelpDialog = false
            }
        )
    }
}
@Composable
fun helpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Got it")
            }
        },
        title = {
            Text("How AI Career Pilot Works")
        },
        text = {
            Column {
                Text("1. Paste the Job Description.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("2. Upload your Resume PDF.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("3. AI analyzes your resume against the job role.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("4. You get Match Score, ATS feedback, strengths, and improvement suggestions.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("5. View your recent analysis history anytime.")
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            placeholder = {
                Text("Paste Job Description...", color = Color.LightGray)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
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
            .clickable(enabled = !isLoading) { onClick() }, // Prevent spam clicks while loading
        contentAlignment = Alignment.Center
    ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FileUpload, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Analyze Match Score", color = Color.Black, fontWeight = FontWeight.Bold)
            }

    }
}

@Composable
fun SectionHeader(
    onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = Color.White
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }

        IconButton(
            onClick = onHelpClick
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Help",
                tint = Color.White
            )
        }
    }
}