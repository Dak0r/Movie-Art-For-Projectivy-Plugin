import android.content.Context
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request

fun downloadImageToUri(context: Context, imageUrl: String, targetUri: Uri): Uri {
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
