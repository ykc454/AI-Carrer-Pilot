package com.nextgendevs.aicareerpilot.util

import android.content.Context
import android.net.Uri

fun getFileName(context: Context, uri: Uri): String {

    var name = "Unknown"

    val cursor = context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )

    cursor?.use {

        val index =
            it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)

        if (it.moveToFirst() && index != -1) {
            name = it.getString(index)
        }
    }

    return name
}