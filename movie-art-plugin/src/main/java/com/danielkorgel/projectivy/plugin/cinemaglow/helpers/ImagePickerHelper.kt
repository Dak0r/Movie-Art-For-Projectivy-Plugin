package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImagePickerHelper {

    private const val CUSTOM_BG_FILENAME = "custom_app_background.jpg"

    /**
     * Get the file where custom background is stored
     */
    fun getCustomBackgroundFile(context: Context): File {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        return File(cacheDir, CUSTOM_BG_FILENAME)
    }

    /**
     * Copy an image from a content URI (gallery pick) to internal storage
     */
    fun copyImageFromUri(context: Context, sourceUri: Uri): File? {
        return try {
            val targetFile = getCustomBackgroundFile(context)
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

    /**
     * Check if a custom background file exists
     */
    fun hasCustomBackground(context: Context): Boolean {
        return getCustomBackgroundFile(context).exists()
    }
}
