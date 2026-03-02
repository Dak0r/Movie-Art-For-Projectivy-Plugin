package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

object BackgroundPickerHelper {

    /**
     * Get the file where custom background is stored
     */
    fun getCustomBackgroundFile(context: Context, fileName: String): File {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        return File(cacheDir, fileName)
    }

    /**
     * Copy an image or video from a content URI to internal storage
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
            
            val targetFile = getCustomBackgroundFile(context, fileName)
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
