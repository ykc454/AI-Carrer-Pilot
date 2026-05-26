package com.example.aicareerpilot.presentation.screens

import android.R.attr.scaleX
import android.R.attr.scaleY
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Color.Black
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
        Color(0xFF4285F4),
        Color(0xFFEA4335),
        Color(0xFFFBBC05),
        Color(0xFF34A853),
        Color(0xFF4285F4)
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
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(18.dp)
                        )
                }
            )

            .clip(RoundedCornerShape(18.dp))

            // Always white background
            .background( Color.Black)

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
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "AI is analyzing...",
                        color = Color.White,
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
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Analyze Match Score",
                        color = Color.White,
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