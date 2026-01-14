package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImagePickerHelper {

    /**
     * Get the file where custom background is stored
     */
    fun getCustomBackgroundFile(context: Context, fileName: String): File {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        return File(cacheDir, fileName)
    }

    /**
     * Copy an image from a content URI (gallery pick) to internal storage
     */
    fun copyImageFromUri(context: Context, sourceUri: Uri): File? {
        return try {
            val fileName = "custom_bg_${System.currentTimeMillis()}.jpg"
            val targetFile = getCustomBackgroundFile(context, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
