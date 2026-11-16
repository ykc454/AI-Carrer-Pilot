package com.nextgendevs.aicareerpilot.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nextgendevs.aicareerpilot.domain.repository.TextRecognitionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device OCR using ML Kit Text Recognition (Latin script, bundled model).
 * Works offline: no image ever leaves the phone. Only the extracted text is
 * later sent to Gemini, exactly like a PDF/DOCX upload.
 */
@Singleton
class MlKitTextRecognitionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : TextRecognitionRepository {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun recognizeText(imagePaths: List<String>): String =
        withContext(Dispatchers.Default) {
            imagePaths.map { path -> recognizePage(path) }
                .filter { it.isNotBlank() }
                .joinToString(separator = "\n\n")
        }

    private suspend fun recognizePage(path: String): String {
        // fromFilePath reads EXIF so rotated photos are handled correctly
        val image = InputImage.fromFilePath(context, Uri.fromFile(File(path)))

        return suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    // Keep ML Kit's block order (top-to-bottom reading order)
                    val text = result.textBlocks.joinToString("\n") { it.text }
                    cont.resume(text)
                }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }
}
