package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun getCacheFile(context: Context, fileName: String): File {
    // Get the external cache directory
    val cacheDir = context.externalCacheDir ?: throw Exception("Can't access external cache dir")

    // Create the file in the external cache directory
    val cacheFile = File(cacheDir, fileName)
    
    // If the file exists, update its last modified timestamp to mark it as "recently used"
    if (cacheFile.exists()) {
        cacheFile.setLastModified(System.currentTimeMillis())
    }

    return cacheFile
}

/**
 * Deletes files in the cache directory that haven't been requested (touched) for [maxAgeDays].
 * Runs in a background thread to avoid blocking the caller.
 */
fun cleanExpiredCache(context: Context, maxAgeDays: Int = 7) {
    thread(start = true, name = "CacheCleanupThread") {
        try {
            val cacheDir = context.externalCacheDir ?: return@thread
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(maxAgeDays.toLong())

            cacheDir.listFiles()?.forEach { file ->
                // Keep the core API cache file regardless of age
                if (file.name == "tmdb_api_cache.json") return@forEach

                if (file.isFile && file.lastModified() < threshold) {
                    println("FileCache: Deleting unused file from cache: ${file.name}")
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun exposeFileToOtherApps(context: Context, cacheFile: File): Uri {

    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)
    context.grantUriPermission(
        "com.spocky.projengmenu", // The receiving app's package name
        fileUri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )

    return fileUri
}

fun downloadFile(context: Context, imageUrl: String, targetUri: Uri): Uri {
    try {
        val client = OkHttpClient()
        val request = Request.Builder().url(imageUrl).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Failed to download image: ${response.code}")

        val inputStream = response.body.byteStream()

        // Write the data directly to the target URI's output stream
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            inputStream.copyTo(outputStream) // Stream the image directly
        } ?: throw Exception("Failed to open output stream for URI: $targetUri")

        inputStream.close()
        return targetUri
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
