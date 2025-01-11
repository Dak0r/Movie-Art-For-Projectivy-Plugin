package tv.projectivy.plugin.wallpaperprovider.cinemaglow.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request


fun getCacheFile(context: Context, fileName: String): File {
    // Get the external cache directory
    val cacheDir = context.externalCacheDir ?: throw Exception("Can't access external cache dir")

    // Create the file in the external cache directory
    val cacheFile = File(cacheDir, fileName)
    // Generate the content URI using FileProvider

    return cacheFile
}

fun exposeFileToOtherApps(context: Context, cacheFile: File): Uri {

    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)
    // Grant read permission to all apps (optional: restrict to specific apps using their package name)
    context.grantUriPermission(
        "com.spocky.projengmenu", // Replace with the receiving app's package name
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

        val inputStream = response.body?.byteStream() ?: throw Exception("Response body is null")

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
