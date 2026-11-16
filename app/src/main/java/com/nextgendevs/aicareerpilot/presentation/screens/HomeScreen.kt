package com.nextgendevs.aicareerpilot.presentation.screens

import com.nextgendevs.aicareerpilot.presentation.theme.AppColors

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgendevs.aicareerpilot.data.model.gemini_response.AnalysisRecord
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeUiState
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeViewModel
import com.nextgendevs.aicareerpilot.util.getFileName
import kotlinx.coroutines.delay
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    resumeViewModel: ResumeViewModel,
    onRecordClick: (AnalysisRecord) -> Unit
) {
    val context = LocalContext.current

    // 1. Observe your true single source of truth from ViewModel
    val uiState by resumeViewModel.uiState.collectAsState()
    val history by resumeViewModel.analysisHistory.collectAsState()
    val jd by resumeViewModel.jobDescription.collectAsState()
    val sharedResume by resumeViewModel.sharedResume.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    // Derived state to determine if we should show loading spinning wheels
    val isLoading = uiState is ResumeUiState.Loading





    // 2. Handle side-effects for Success and Error states cleanly
    LaunchedEffect(uiState) {
        when (uiState) {
            is ResumeUiState.Success -> {

                Toast.makeText(
                    context,
                    "Analysis Complete!",
                    Toast.LENGTH_SHORT
                ).show()

                delay(500)

                history.firstOrNull()?.let { newestRecord ->
                    onRecordClick(newestRecord)
                }

                resumeViewModel.resetUiState()
            }
            is ResumeUiState.Error -> {
                val errorMessage = (uiState as ResumeUiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                resumeViewModel.resetUiState()
            }
            else -> {}
        }
    }

    // Camera scanner for paper resumes (CameraX + ML Kit OCR)
    var showScanner by remember { mutableStateOf(false) }
    val openScanner = {
        if (jd.isBlank()) {
            Toast.makeText(context, "Please paste job description", Toast.LENGTH_SHORT).show()
        } else {
            showScanner = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        uri?.let {

            val fileName = getFileName(context, it)

            resumeViewModel.processResume(
                it,
                fileName
            )
        }
    }

    // Soft accent glow (captured here: drawBehind is not a composable scope)
    val glow = AppColors.Accent.copy(alpha = if (AppColors.IsDark) 0.07f else 0.06f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                AppColors.Background
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glow,
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
            //TABLET UI
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
                    sharedResume?.let { shared ->
                        SharedResumeCard(
                            fileName = shared.fileName,
                            canAnalyze = jd.isNotBlank() && !isLoading,
                            onAnalyze = { resumeViewModel.analyzeSharedResume() },
                            onCancel = { resumeViewModel.dismissSharedResume() }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    JDCard(jd) { resumeViewModel.updateJD(it) }
                }

                // RIGHT SIDE
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f)
                ) {
                    UploadButtonPremium(isLoading) {
                        launcher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    ScanResumeButton(enabled = !isLoading) { openScanner() }

                    Spacer(Modifier.height(24.dp))

                    val latest = history.firstOrNull()
                    latest?.let { record ->
                        HistoryCard(
                            record = record,
                            onClick = { onRecordClick(record) },
                            viewModel = resumeViewModel
                        )
                    }
                }
            }
        } else {
            //PHONE UI
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 80.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    sharedResume?.let { shared ->
                        item {
                            SharedResumeCard(
                                fileName = shared.fileName,
                                canAnalyze = jd.isNotBlank() && !isLoading,
                                onAnalyze = { resumeViewModel.analyzeSharedResume() },
                                onCancel = { resumeViewModel.dismissSharedResume() }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    item { JDCard(jd) { resumeViewModel.updateJD(it) } }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    item {
                        UploadButtonPremium(isLoading) {
                            if (jd.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please paste job description",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                launcher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    )
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    item { ScanResumeButton(enabled = !isLoading) { openScanner() } }
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
                                viewModel = resumeViewModel
                            )
                        } else {
                            Text(
                                text = "No recent activity",
                                color = AppColors.TextSecondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

            }
        }
    }
    if (showScanner) {
        ResumeScannerScreen(
            onScanComplete = { pagePaths ->
                showScanner = false
                resumeViewModel.processScannedResume(pagePaths)
            },
            onDismiss = { showScanner = false }
        )
    }
    if (showHelpDialog) {
        HelpDialog(
            onDismiss = {
                showHelpDialog = false
            }
        )
    }
}

// Shown when a resume was shared into the app from another app
@Composable
fun SharedResumeCard(
    fileName: String,
    canAnalyze: Boolean,
    onAnalyze: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.TextPrimary.copy(alpha = 0.08f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "📄 Resume received",
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = fileName,
                color = AppColors.TextPrimary.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Paste the job description below, then tap Analyze.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextPrimary.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = AppColors.TextPrimary.copy(alpha = 0.7f))
                }
                Button(
                    onClick = onAnalyze,
                    enabled = canAnalyze,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.TextPrimary,
                        contentColor = AppColors.Background
                    )
                ) {
                    Text("Analyze")
                }
            }
        }
    }
}

@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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

                Text("2. Upload your Resume PDF or DOCX.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("3. AI analyzes your resume against the job role.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("4. You get Match Score, ATS feedback, strengths, and improvement suggestions.")
                Spacer(modifier = Modifier.height(8.dp))

                Text("5. View your recent analysis history anytime.")
            }
        },
        containerColor = AppColors.Surface,
        titleContentColor = AppColors.TextPrimary,
        textContentColor = AppColors.TextSecondary
    )
}

@Composable
fun JDCard(jd: String, onChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = jd,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp, max = 200.dp)
                .padding(8.dp),

            shape = RoundedCornerShape(20.dp),

            placeholder = {
                Text(
                    "Paste Job Description...",
                    color = AppColors.TextSecondary
                )
            },

            trailingIcon = {

                if (jd.isNotEmpty()) {

                    IconButton(
                        onClick = {
                            onChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Text",
                            tint = AppColors.TextPrimary
                        )
                    }
                }
            },

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),

            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),

            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                focusedBorderColor = AppColors.TextPrimary,
                unfocusedBorderColor = AppColors.TextSecondary
            )
        )
    }
}

@Composable
fun ScanResumeButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.TextPrimary.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary)
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.DocumentScanner,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text("Scan paper resume")
    }
}

@Composable
fun UploadButtonPremium(
    isLoading: Boolean,
    onClick: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "premium_border")

    // Smooth rotating border animation
    val degrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Google-inspired gradient colors
    val googleGradient = listOf(
        AppColors.Accent,
        AppColors.AccentSoft,
        AppColors.Accent.copy(alpha = 0.6f),
        AppColors.AccentSoft,
        AppColors.Accent
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)

            // Animated border ONLY during loading
            .then(
                if (isLoading) {

                    Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .drawBehind {
                            rotate(
                                degrees = degrees,
                                pivot = center
                            ) {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = googleGradient,
                                        center = center
                                    ),
                                    radius = size.maxDimension
                                )
                            }
                        }
                        .padding(2.dp)

                } else {

                    Modifier
                        .border(
                            width = 1.dp,
                            color = AppColors.TextPrimary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(18.dp)
                        )
                }
            )

            .clip(RoundedCornerShape(18.dp))

            // Always white background
            .background( AppColors.Background)

            .clickable(
                enabled = !isLoading
            ) {
                onClick()
            },

        contentAlignment = Alignment.Center
    ) {

        AnimatedContent(
            targetState = isLoading,

            transitionSpec = {
                fadeIn(
                    animationSpec = tween(220)
                ) togetherWith fadeOut(
                    animationSpec = tween(180)
                )
            },

            label = "button_content"
        ) { loading ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                if (loading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AppColors.TextPrimary,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "AI is analyzing...",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(
                            fontSize = 16.sp,
                            letterSpacing = 0.3.sp
                        )
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Upload",
                        tint = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Upload Resume for Analysis",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            fontSize = 16.sp,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }
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
                tint = AppColors.TextPrimary
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.TextPrimary
            )
        }

        IconButton(
            onClick = onHelpClick
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = "Help",
                tint = AppColors.TextPrimary
            )
        }
    }
}

@Composable
fun CreditInfoDialog(
    remainingAttempts: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,

        title = {
            Text(
                text = "Daily Credits",
                color = AppColors.TextPrimary
            )
        },

        text = {
            Column {
                Text(
                    text = "You receive 3 resume analyses every day.",
                    color = AppColors.TextSecondary
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "• 1 analysis = 1 credit",
                    color = AppColors.TextSecondary
                )

                Text(
                    text = "• Credits reset automatically every day",
                    color = AppColors.TextSecondary
                )

                Text(
                    text = "• Remaining today: $remainingAttempts / 3",
                    color = AppColors.TextPrimary
                )
            }
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}