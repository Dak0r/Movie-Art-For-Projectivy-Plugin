package tv.projectivy.plugin.wallpaperprovider.fanart_wallpaper

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import java.io.File

class WallpaperProviderService: Service() {

    val that = this

    fun getUriForCacheFile(context: Context, fileName: String): Uri {
        val cacheFile = File(context.cacheDir, fileName) // Get the file in cache directory
        return Uri.fromFile(cacheFile) // Convert to Uri
    }

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
                is Event.TimeElapsed -> {
                    // This is where you generate the wallpaper list that will be cycled every x minute
                    return listOf(
                        // DRAWABLE can be served from app drawable/
                        Wallpaper(getDrawableUri(R.drawable.ic_banner_drawable).toString(), WallpaperType.DRAWABLE),
                        // IMAGE can be served from app drawable/, local storage or internet
                        Wallpaper(PreferencesManager.imageUrl, WallpaperType.IMAGE, author = "Pixabay"),
                        // ANIMATED_DRAWABLE can be served from app drawable/
                        Wallpaper(getDrawableUri(R.drawable.anim_sample).toString(), WallpaperType.ANIMATED_DRAWABLE),
                        // LOTTIE can be served from app raw/, local storage or internet
                        Wallpaper(getDrawableUri(R.raw.gradient).toString(), WallpaperType.LOTTIE),
                        // VIDEO can be served from app raw/, local storage or internet (some formats might not be supported, though)
                        Wallpaper(getDrawableUri(R.raw.light).toString(), WallpaperType.VIDEO)
                    )
                }

                // Below are "dynamic events" that might interest you in special cases
                // You will only receive dynamic events depending on the updateMode declared in your manifest
                // Don't subscribe if not interested :
                //  - this will consume device resources unnecessarily
                //  - some cache optimizations won't be enabled for dynamic wallpaper providers

                // When the focused card changes
                is Event.CardFocused -> {
                    try {
                        val file = getCacheFile(
                            that,
                            "gradient_${event.lightColor}_${event.darkColor}.json"
                        )
                        //if (!fileUriExists(that, fileUri)) {
                        if(true) {
                            val lottieEditor = LottieEditorRegex(that, getDrawableUri(R.raw.two_color_gradient))
                            lottieEditor
                                .load()
                                .replaceGradientColors(
                                    //positions = listOf(0f, 0.5f, 1f),
                                    //colors = listOf(0xFF0000FF.toInt(), 0xFF00FFFF.toInt(), 0xFFFF00FF.toInt()) // Blue, Green, Red
                                    positions = listOf(0f, 1f), // Positions
                                    colors = listOf(event.lightColor, event.darkColor) // Colors
                                )
                                .save(Uri.fromFile(file))
                        }
                        val shareableUri = exposeFileToOtherApps(that, file)
                        return listOf(
                            //Wallpaper(getDrawableUri(R.raw.gradient).toString(), WallpaperType.LOTTIE),
                            Wallpaper(shareableUri.toString(), WallpaperType.LOTTIE),
                        )
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return listOf(
                        Wallpaper(getDrawableUri(R.drawable.anim_sample).toString(), WallpaperType.ANIMATED_DRAWABLE)
                    )
                }

                // When the focused "program" card changes
                is Event.ProgramCardFocused -> {
                    event.title?.let{ it ->
                        val tmdbApiKey = "" // Replace with your TMDb API Key
                        val tmdbApi = TMDbApi(tmdbApiKey)

                        try {
                            val backgroundImageUrl = tmdbApi.fetchBackgroundImage(cleanString(it))
                            if (backgroundImageUrl != null) {
                                println("Background image URL: $backgroundImageUrl")
                                return listOf(
                                    Wallpaper(backgroundImageUrl, WallpaperType.IMAGE, author = "The Movie Database")
                                )
                            } else {
                                println("No background image found for the title: $it")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        event.iconUri?.let { iconUri ->
                            return listOf(
                                Wallpaper(iconUri, WallpaperType.IMAGE)
                            )
                        }
                        return emptyList()
                    }

                    return emptyList()
                }
                // When Projectivy enters or exits idle mode
                is Event.LauncherIdleModeChanged -> {
                    return if (event.isIdle) { listOf(Wallpaper(getDrawableUri(R.drawable.ic_plugin).toString(), WallpaperType.DRAWABLE)) }
                        else  emptyList()
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
            val noBrackets = input.replace("\\[.*?\\]".toRegex(), " ")
            // Replace all special characters with spaces
            val noSpecialChars = noBrackets.replace("[^a-zA-Z0-9]".toRegex(), " ")
            // Replace multiple sequential spaces with a single space
            return noSpecialChars.replace("\\s+".toRegex(), " ").trim()
        }


    }
}