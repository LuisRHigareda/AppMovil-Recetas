package com.example.recetario.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun saveBitmapToInternalStorage(
    context: Context,
    bitmap: Bitmap,
    filePrefix: String
): String? {
    val imagesDirectory = File(context.filesDir, "images")

    if (!imagesDirectory.exists()) {
        imagesDirectory.mkdirs()
    }

    val imageFile = File(
        imagesDirectory,
        "${filePrefix}_${System.currentTimeMillis()}.jpg"
    )

    return runCatching {
        FileOutputStream(imageFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
        }

        Uri.fromFile(imageFile).toString()
    }.getOrNull()
}
