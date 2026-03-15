package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

object BackgroundPickerHelper {

    /**
     * Get the file where custom background is stored.
     * Checks persistent storage first, then falls back to cache for migration.
     */
    fun getCustomBackgroundFile(context: Context, fileName: String): File {
        // Fallback for migration: check if it exists in cache
        val cacheFile = File(context.externalCacheDir ?: context.cacheDir, fileName)
        if (cacheFile.exists()) {
            return cacheFile
        }

        // Default to persistent location for new files
        val persistentFile = File(context.getExternalFilesDir(null) ?: context.filesDir, fileName)
        return persistentFile
    }

    /**
     * Copy an image or video from a content URI to internal storage (persistent)
     */
    fun copyBackgroundFromUri(context: Context, sourceUri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            
            val fileName = if (extension != null) {
                "custom_bg_${System.currentTimeMillis()}.$extension"
            } else {
                sourceUri.lastPathSegment ?: "custom_bg_${System.currentTimeMillis()}"
            }
            
            // New files are always saved to the persistent location
            val targetFile = File(context.getExternalFilesDir(null) ?: context.filesDir, fileName)
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
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
