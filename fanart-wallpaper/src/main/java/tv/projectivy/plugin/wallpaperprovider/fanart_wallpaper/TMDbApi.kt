package tv.projectivy.plugin.wallpaperprovider.fanart_wallpaper

import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.Locale

class TMDbApi(private val apiKey: String) {

    private val client = OkHttpClient()
    private val gson = Gson()

    // Base URL for TMDb API
    private val baseUrl = "https://api.themoviedb.org/3"

    /**
     * Fetches a movie or TV show's background image URL based on the title with prioritized sorting.
     *
     * @param title The title of the movie or TV show.
     * @return The URL of the background image or null if not found.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchBackgroundImage(title: String): String? {
        val language = Locale.getDefault().language
        val searchUrl = "$baseUrl/search/multi?api_key=$apiKey&page=1&language=$language&query=${java.net.URLEncoder.encode(title, "UTF-8")}"

        val searchResponse = client.newCall(Request.Builder().url(searchUrl).build()).execute()
        if (!searchResponse.isSuccessful) throw Exception("Failed to fetch search results: ${searchResponse.code}")

        val searchResult = gson.fromJson(searchResponse.body?.string(), SearchResult::class.java)

        // Select the best match
        val bestMatch = searchResult.results.firstOrNull() ?: return null

        // Fetch the backdrop image
        return bestMatch.backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }
    }

    // Data classes for parsing TMDb API response
    data class SearchResult(
        val results: List<Result>
    )

    data class Result(
        @SerializedName("backdrop_path") val backdropPath: String?,
        val title: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("media_type") val mediaType: String?, // e.g., "movie" or "tv"
        val popularity: Double?,
        @SerializedName("release_date") val releaseDate: String? // Format: YYYY-MM-DD
    )
}