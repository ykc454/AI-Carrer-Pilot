package com.nextgendevs.aicareerpilot.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Full-screen camera scanner for paper resumes.
 *
 * Pure UI: it only captures page photos into the app cache and hands their
 * paths back through [onScanComplete]. OCR and analysis happen in the
 * ViewModel -> ScanResumeUseCase -> TextRecognitionRepository chain.
 */
@Composable
fun ResumeScannerScreen(
    onScanComplete: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pages = remember { mutableStateListOf<String>() }

    // Delete captured photos if the user closes the scanner without analysing
    fun cancel() {
        pages.forEach { File(it).delete() }
        onDismiss()
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Dialog(
        onDismissRequest = { cancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasPermission) {
                CameraContent(
                    pageCount = pages.size,
                    onPageCaptured = { pages.add(it) },
                    onDone = { onScanComplete(pages.toList()) }
                )
            } else {
                PermissionRationale(
                    onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }

            IconButton(
                onClick = { cancel() },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close scanner", tint = Color.White)
            }
        }
    }
}

@Composable
private fun CameraContent(
    pageCount: Int,
    onPageCaptured: (String) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCapturing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // sharper text = better OCR
            .build()
    }
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    // Bind Preview + ImageCapture to the lifecycle (suspend API, no Guava needed)
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    LaunchedEffect(lifecycleOwner) {
        val provider = awaitCameraProvider(context)
        cameraProvider = provider
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (_: Exception) {
            Toast.makeText(context, "Camera unavailable", Toast.LENGTH_SHORT).show()
        }
    }
    // Release the camera when leaving the screen
    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Page guide frame
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .aspectRatio(1f / 1.414f) // A4 ratio
                .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .navigationBarsPadding()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (pageCount == 0) "Fit one resume page inside the frame"
                else "$pageCount page(s) captured. Add the next page or tap Done",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(56.dp))

                // Shutter button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .background(
                            if (isCapturing) Color.Gray else Color.White,
                            CircleShape
                        )
                        .clickable(enabled = !isCapturing) {
                            isCapturing = true
                            capturePage(
                                context = context,
                                imageCapture = imageCapture,
                                pageIndex = pageCount + 1,
                                onSaved = { path ->
                                    isCapturing = false
                                    onPageCaptured(path)
                                },
                                onError = {
                                    isCapturing = false
                                    Toast.makeText(context, "Capture failed, try again", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                )

                FilledIconButton(
                    onClick = onDone,
                    enabled = pageCount > 0 && !isCapturing,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Done, analyse scanned pages")
                }
            }
        }
    }
}

/** Suspends until CameraX's ProcessCameraProvider is ready. */
private suspend fun awaitCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                cont.resume(future.get())
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
        cont.invokeOnCancellation { future.cancel(false) }
    }

/** Saves one photo to cache/scans/ and returns its path. */
private fun capturePage(
    context: Context,
    imageCapture: ImageCapture,
    pageIndex: Int,
    onSaved: (String) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val dir = File(context.cacheDir, "scans").apply { mkdirs() }
    val file = File(dir, "page_${System.currentTimeMillis()}_$pageIndex.jpg")
    val output = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        output,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(results: ImageCapture.OutputFileResults) =
                onSaved(file.absolutePath)

            override fun onError(exception: ImageCaptureException) = onError(exception)
        }
    )
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Camera access is needed to scan a paper resume. Photos stay on your device; only the recognised text is analysed.",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequest) { Text("Allow camera") }
    }
}
