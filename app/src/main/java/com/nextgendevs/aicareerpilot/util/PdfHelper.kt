package com.nextgendevs.aicareerpilot.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream

class DocumentHelper(
    private val context: Context
) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractTextFromUri(
        uri: Uri,
        fileName: String
    ): String = withContext(Dispatchers.IO) {

        try {

            val extension = fileName
                .substringAfterLast('.', "")
                .lowercase()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                return@withContext when (extension) {

                    "pdf" -> extractPdfText(inputStream)

                    "docx" -> extractDocxText(inputStream)

                    else -> "Unsupported file format"
                }
            }

            "Could not open file"

        } catch (e: Exception) {

            "Error: ${e.localizedMessage}"
        }
    }

    private fun extractPdfText(
        inputStream: InputStream
    ): String {

        PDDocument.load(inputStream).use { document ->

            val stripper = PDFTextStripper()

            return stripper.getText(document)
        }
    }

    private fun extractDocxText(
        inputStream: InputStream
    ): String {

        XWPFDocument(inputStream).use { document ->

            val paragraphs = document.paragraphs

            return paragraphs.joinToString("\n") {
                it.text
            }
        }
    }

}