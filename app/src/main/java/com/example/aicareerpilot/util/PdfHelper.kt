package com.example.aicareerpilot.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfHelper(private val context: Context) {

    init {
        // Required for PDFBox to work on Android
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractTextFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                document.close()
                return@withContext text
            } ?: "Error: Could not open file"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}

