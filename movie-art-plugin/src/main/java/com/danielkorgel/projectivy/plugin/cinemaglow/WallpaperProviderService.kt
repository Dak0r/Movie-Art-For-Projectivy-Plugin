package com.danielkorgel.projectivy.plugin.cinemaglow

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.ApiResponseCache
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.LottieEditorRegex
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.TMDbApi
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.downloadFile
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.exposeFileToOtherApps
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.getCacheFile
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType

class WallpaperProviderService: Service() {

    val that = this
    var apiCache: ApiResponseCache? = null

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
        apiCache = ApiResponseCache(this,
            Uri.fromFile(getCacheFile(this,"tmdb_api_cache.json")))
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
                    event.title?.let { title ->
                        val cleanName = cleanString(title)
                        val file = getCacheFile(
                            that,
                            "backdrop_${cleanName}.jpg"
                        )
                        if (!fileUriExists(that, Uri.fromFile(file))) {
                            try {
                                var downloadUrl: String? = null
                                apiCache?.let { cache ->
                                    if (cache.containsKey(cleanName)) {
                                        downloadUrl = cache.get(cleanName)
                                        println("Found Cached Api Response for $title -> $downloadUrl")
                                    }
                                }

                                if(downloadUrl == null) {
                                    val tmdbApi = TMDbApi(BuildConfig.TMDB_API_KEY)
                                    val backgroundImageUrl =
                                        tmdbApi.fetchBackgroundImage(cleanName)
                                    if (backgroundImageUrl != null) {
                                        println("TMDB Background image URL: $backgroundImageUrl")
                                        apiCache?.put(cleanName, backgroundImageUrl)
                                        downloadUrl = backgroundImageUrl
                                    } else {
                                        println("TMDB: No background image found for the title: $title ($cleanName)")
                                        apiCache?.put(cleanName, "None")
                                    }
                                }
                                downloadUrl?.let { url ->
                                    if (url != "None") {
                                        downloadFile(
                                            that,
                                            url,
                                            Uri.fromFile(file)
                                        )
                                        println("Download done: ${file.path}")
                                    }
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
            val noSpecialChars = java.net.URLEncoder.encode(noBrackets, "utf-8")
            // Replace multiple sequential spaces with a single space
            return noSpecialChars.replace("\\s+".toRegex(), " ").trim()
        }


    }
}