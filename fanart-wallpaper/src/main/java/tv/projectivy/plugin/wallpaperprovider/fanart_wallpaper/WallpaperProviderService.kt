package tv.projectivy.plugin.wallpaperprovider.fanart_wallpaper

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import downloadImageToUri
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType

class WallpaperProviderService: Service() {

    val that = this

    fun fileUriExists(context: Context, fileUri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(fileUri)?.close() // Try opening the URI
            true // If no exception, file exists
        } catch (e: Exception) {
            false // If an exception occurs, file does not exist
        }
    }

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun getWallpapers(event: Event?): List<Wallpaper> {

            return when (event) {
                // When the focused card changes
                is Event.CardFocused -> {
                    try {
                        val file = getCacheFile(
                            that,
                            "gradient_${event.lightColor}_${event.darkColor}.json"
                        )
                        if (!fileUriExists(that, Uri.fromFile(file))) {
                            val lottieEditor = LottieEditorRegex(that, getDrawableUri(R.raw.two_color_gradient))
                            lottieEditor
                                .load()
                                .replaceGradientColors(
                                    positions = listOf(0f, 1f), // Positions
                                    colors = listOf(event.lightColor, event.darkColor) // Colors
                                )
                                .save(Uri.fromFile(file))
                        }
                        val shareableUri = exposeFileToOtherApps(that, file)
                        return listOf(
                            Wallpaper(shareableUri.toString(), WallpaperType.LOTTIE),
                        )
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return listOf(
                        Wallpaper(getDrawableUri(R.raw.gradient).toString(), WallpaperType.LOTTIE)
                    )
                }

                // When the focused "program" card changes
                is Event.ProgramCardFocused -> {
                    event.title?.let{
                        val cleanName = cleanString(it)
                        val file = getCacheFile(
                            that,
                            "backdrop_${cleanName}.jpg"
                        )
                        if (!fileUriExists(that, Uri.fromFile(file))) {
                            val tmdbApiKey =
                                "" // Replace with your TMDb API Key
                            val tmdbApi = TMDbApi(tmdbApiKey)

                            try {
                                val backgroundImageUrl =
                                    tmdbApi.fetchBackgroundImage(cleanName)
                                if (backgroundImageUrl != null) {
                                    println("Background image URL: $backgroundImageUrl")
                                    downloadImageToUri(that, backgroundImageUrl, Uri.fromFile(file))
                                    println("Download done: ${file.path}")
                                } else {
                                    println("No background image found for the title: $it")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        if (fileUriExists(that, Uri.fromFile(file))) {
                            val shareableUri = exposeFileToOtherApps(that, file)
                            return listOf(
                                Wallpaper(
                                    shareableUri.toString(),
                                    WallpaperType.IMAGE,
                                    author = "themoviedb.org"
                                )
                            )
                        }
                    }
                    event.iconUri?.let { iconUri ->
                        return listOf(
                            Wallpaper(iconUri, WallpaperType.IMAGE)
                        )
                    }

                    return listOf(
                        Wallpaper(getDrawableUri(R.raw.gradient).toString(), WallpaperType.LOTTIE)
                    )
                }

                else -> emptyList()  // Returning an empty list won't change the currently displayed wallpaper
            }
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }

        fun getDrawableUri(drawableId: Int): Uri {
            return Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(drawableId))
                .appendPath(resources.getResourceTypeName(drawableId))
                .appendPath(resources.getResourceEntryName(drawableId))
                .build()
        }

        fun cleanString(input: String): String {
            // Remove content within square brackets (including the brackets)
            val noBrackets = input.replace("\\[.*?]".toRegex(), " ")
            // Replace all special characters with spaces
            val noSpecialChars = noBrackets.replace("[^a-zA-Z0-9]".toRegex(), " ")
            // Replace multiple sequential spaces with a single space
            return noSpecialChars.replace("\\s+".toRegex(), " ").trim()
        }


    }
}