package com.danielkorgel.projectivy.plugin.cinemaglow

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.PowerManager
import android.webkit.MimeTypeMap
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.ApiResponseCache
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.BackgroundPickerHelper
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.ImageProcessor
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.LottieEditorRegex
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.TMDbApi
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.cleanExpiredCache
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.downloadBitmap
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.exposeFileToOtherApps
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.getCacheFile
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.saveBitmapToFile
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import java.util.concurrent.CompletableFuture

@Suppress("WrongConstant") // false positive linting issue for WallpaperType
class WallpaperProviderService : Service() {

    val that = this
    var apiCache: ApiResponseCache? = null
    var tmdbApi: TMDbApi? = null



    fun fileUriExists(context: Context, fileUri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(fileUri)?.close() // Try opening the URI
            true // If no exception, file exists
        } catch (_: Exception) {
            false // If an exception occurs, file does not exist
        }
    }

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
        apiCache = ApiResponseCache(
            this,
            Uri.fromFile(getCacheFile(this, "tmdb_api_cache.json"))
        )
        tmdbApi = TMDbApi(BuildConfig.TMDB_API_KEY, apiCache)

        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        if (currentTime - PreferencesManager.lastCacheClean > oneDayInMillis) {
            println("Cleaning Cache once in 24hrs")
            cleanExpiredCache(this)
            PreferencesManager.lastCacheClean = currentTime
        }

        println("Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // When the service is destroyed, check if the screen is off to detect sleep states.
        // If it is off, we should reset the wallpaper
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) {
            println("Service destroyed while screen is OFF. Assuming Sleep or Power off. Reset Last Wallpaper.")
            PreferencesManager.lastWallpaper = ""
        } else {
            println("Service destroyed while screen is ON (likely app switch).")
        }
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> {

            // Check if Projectivy was restarted since the last call:
            val currentPid = getCallingPid()
            val lastPid = PreferencesManager.lastCallingPid
            if (lastPid != currentPid) {
                // If the app was restarted, we can't be sure that it remembers the the last wallpaper
                // we sent. So we always send it in that case.
                PreferencesManager.lastWallpaper = ""
                // After a full device reboot, the first call to set a larger video wallpaper sometimes fails.
                // So we always send videos for the first few calls after Projectivy was started.
                // (accepting that this is also triggered on every app restart, but reboots are more likely for a luncher I guess)
                PreferencesManager.videoForcedUpdateCount = 2

                PreferencesManager.lastCallingPid = currentPid
            }

            return when (event) {
                // When the focused card changes (app icons)
                is Event.CardFocused -> {
                    fallbackWallpaper(event)
                }

                // When the focused "program" card changes
                is Event.ProgramCardFocused -> {
                    val api = tmdbApi
                    event.title?.let { title ->
                        val info = api?.searchArtInfo(title)
                        var file: java.io.File? = null
                        if (info?.backdropUrl != null) {
                            val filename = info.backdropUrl.substringAfterLast("/")
                            file = getCacheFile(that, "processed_v4_$filename")
                            
                            if (!fileUriExists(that, Uri.fromFile(file))) {
                                try {
                                    val logoUrl = api.fetchLogoUrl(info.mediaType, info.id)
                                    
                                    val backdropFuture = CompletableFuture.supplyAsync { downloadBitmap(info.backdropUrl) }
                                    val logoFuture = logoUrl?.let { url -> 
                                        CompletableFuture.supplyAsync { downloadBitmap(url) }
                                    }

                                    val backdropBitmap = backdropFuture.get()
                                    val logoBitmap = logoFuture?.get()

                                    if (backdropBitmap != null) {
                                        val processedBitmap = ImageProcessor.processMovieArt(
                                            backdropBitmap,
                                            logoBitmap,
                                            addShadow = true
                                        )
                                        saveBitmapToFile(processedBitmap, file)
                                        backdropBitmap.recycle()
                                        logoBitmap?.recycle()
                                        processedBitmap.recycle()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (file != null && fileUriExists(that, Uri.fromFile(file))) {
                            val shareableUri = exposeFileToOtherApps(that, file).toString()
                            updateLastWallpaperSent(shareableUri)
                            return listOf(
                                Wallpaper(
                                    shareableUri,
                                    WallpaperType.IMAGE,
                                    author = "themoviedb.org"
                                )
                            )
                        }
                    }

                    // Fallback to icon of the card
                    event.iconUri?.let { iconUri ->
                        updateLastWallpaperSent(iconUri)
                        return listOf(
                            Wallpaper(iconUri, WallpaperType.IMAGE)
                        )
                    }

                    fallbackWallpaper(event)
                }
                // It's unexpected that we receive any other kind of event, but in case we do we ignore it.
                // Returning an empty list won't change the currently displayed wallpaper.
                else -> emptyList()
            }
        }

        private fun updateLastWallpaperSent(uri: String) {
            PreferencesManager.lastWallpaper = uri
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

        private fun isVideoFile(fileName: String): Boolean {
            val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            return mimeType?.startsWith("video/") == true
        }

        private fun fallbackWallpaper(
            event: Event
        ): List<Wallpaper> {

            // Check if custom background is enabled and exists
            if (PreferencesManager.fallbackBackground == PreferencesManager.FallbackBackground.CustomBackground
                && PreferencesManager.customFallbackBackgroundName != null) {
                val fileName = PreferencesManager.customFallbackBackgroundName!!
                val customBgFile = BackgroundPickerHelper.getCustomBackgroundFile(that, fileName)
                if (customBgFile.exists()) {
                    try {
                        val shareableUri = exposeFileToOtherApps(that, customBgFile).toString()
                        val type: @WallpaperType Int =
                            if (isVideoFile(fileName)) WallpaperType.VIDEO else WallpaperType.IMAGE

                        // Don't Resend wallpaper if it's a VIDEO that hasn't changed.
                        if (type == WallpaperType.VIDEO && PreferencesManager.lastWallpaper == shareableUri) {
                            if (PreferencesManager.videoForcedUpdateCount > 0) {
                                PreferencesManager.videoForcedUpdateCount--
                                println("Force update count: ${PreferencesManager.videoForcedUpdateCount}")
                            } else {
                                // To prevent videos from restarting on every card change we return empty list,
                                // if we return a custom video multiples in a row
                                println("Returning emptyList to prevent video from getting restarted")
                                return emptyList()
                            }
                        }

                        updateLastWallpaperSent(shareableUri)
                        println("Returning wallpaper")
                        return listOf(
                            Wallpaper(shareableUri, type)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fall through to color extraction
                    }
                }
            }

            if (PreferencesManager.fallbackBackground == PreferencesManager.FallbackBackground.PopularMoviesAndShows) {
                try {
                    val api = tmdbApi
                    var info: TMDbApi.SearchResultInfo? = null
                    var maxTries = 5
                    val popularInfo = api?.fetchPopularTitlesInfo(TMDbApi.TimeWindow.DAY)
                    do {
                        info = popularInfo?.random()
                        maxTries--
                    } while ((info == null || info.backdropUrl == PreferencesManager.lastWallpaper) && maxTries > 0)

                    if (info?.backdropUrl != null) {
                        val filename = info.backdropUrl.substringAfterLast("/")
                        val file = getCacheFile(that, "processed_v4_$filename")
                        if (!fileUriExists(that, Uri.fromFile(file))) {
                            val logoUrl = api?.fetchLogoUrl(info.mediaType, info.id)
                            
                            val backdropFuture = CompletableFuture.supplyAsync { downloadBitmap(info.backdropUrl) }
                            val logoFuture = logoUrl?.let { url ->
                                CompletableFuture.supplyAsync { downloadBitmap(url) }
                            }

                            val backdropBitmap = backdropFuture.get()
                            val logoBitmap = logoFuture?.get()

                            if (backdropBitmap != null) {
                                val processedBitmap = ImageProcessor.processMovieArt(
                                    backdropBitmap,
                                    logoBitmap,
                                    addShadow = true
                                )
                                saveBitmapToFile(processedBitmap, file)
                                backdropBitmap.recycle()
                                logoBitmap?.recycle()
                                processedBitmap.recycle()
                            }
                        }
                        if (fileUriExists(that, Uri.fromFile(file))) {
                            val shareableUri = exposeFileToOtherApps(that, file).toString()
                            updateLastWallpaperSent(shareableUri)
                            return listOf(
                                Wallpaper(
                                    shareableUri,
                                    WallpaperType.IMAGE,
                                    author = "themoviedb.org"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback to dynamic colors
            if (event is Event.CardFocused) {
                try {
                    val file = getCacheFile(
                        that,
                        "gradient_${event.lightColor}_${event.darkColor}.json"
                    )
                    if (!fileUriExists(that, Uri.fromFile(file))) {
                        val lottieEditor =
                            LottieEditorRegex(that, getDrawableUri(R.raw.two_color_gradient))
                        lottieEditor
                            .load()
                            .replaceGradientColors(
                                positions = listOf(0f, 1f), // Positions
                                colors = listOf(event.lightColor, event.darkColor) // Colors
                            )
                            .save(Uri.fromFile(file))
                    }
                    val shareableUri = exposeFileToOtherApps(that, file).toString()
                    updateLastWallpaperSent(shareableUri)
                    return listOf(
                        Wallpaper(shareableUri, WallpaperType.LOTTIE),
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Final fallback: return default gradient
            val defaultUri = getDrawableUri(R.raw.gradient).toString()
            updateLastWallpaperSent(defaultUri)
            return listOf(
                Wallpaper(defaultUri, WallpaperType.LOTTIE)
            )
        }

    }
}
