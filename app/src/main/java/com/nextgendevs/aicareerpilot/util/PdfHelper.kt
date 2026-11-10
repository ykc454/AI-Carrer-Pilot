package com.nextgendevs.aicareerpilot.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

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

        // A .docx is a zip; body text lives in word/document.xml.
        // Stream-parse it instead of pulling in Apache POI (huge + memory hungry).
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    return parseDocumentXml(zip)
                }
                entry = zip.nextEntry
            }
        }
        return ""
    }

    private fun parseDocumentXml(xml: InputStream): String {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            setInput(xml, "UTF-8")
        }
        val out = StringBuilder()
        var inText = false

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> if (parser.namespace == W_NS) {
                    when (parser.name) {
                        "t" -> inText = true
                        "tab" -> out.append('\t')
                        "br", "cr" -> out.append('\n')
                    }
                }
                XmlPullParser.TEXT -> if (inText) out.append(parser.text)
                XmlPullParser.END_TAG -> if (parser.namespace == W_NS) {
                    when (parser.name) {
                        "t" -> inText = false
                        "p" -> out.append('\n')
                    }
                }
            }
            event = parser.next()
        }
        return out.toString().trimEnd()
    }

    private companion object {
        const val W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    }

}