package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.danielkorgel.projectivy.plugin.cinemaglow.PreferencesManager
import java.io.File
import java.io.FileOutputStream
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun getCacheDir(context: Context): File {
    return context.externalCacheDir ?: context.cacheDir
}

fun getCacheFile(context: Context, fileName: String): File {
    val cacheDir = getCacheDir(context)
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
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(maxAgeDays.toLong())
            val dirsToClean = listOfNotNull(context.externalCacheDir, context.cacheDir).distinct()

            for (cacheDir in dirsToClean) {
                cacheDir.listFiles()?.forEach { file ->
                    // Keep the core API cache file and custom background regardless of age
                    if (file.name == "tmdb_api_cache.json") return@forEach
                    if (file.name == PreferencesManager.customFallbackBackgroundName) return@forEach

                    if (file.isFile && file.lastModified() < threshold) {
                        println("FileCache: Deleting unused file from cache: ${file.name}")
                        file.delete()
                    }
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

val sharedOkHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

fun downloadBitmap(imageUrl: String, client: OkHttpClient = sharedOkHttpClient): Bitmap? {
    return try {
        val request = Request.Builder().url(imageUrl).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null
        val inputStream = response.body.byteStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
    return try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
