package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.os.Build
import androidx.annotation.RequiresApi
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.Utils.cleanString
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.Locale

class TMDbApi(private val apiKey: String, private val apiCache: ApiResponseCache?) {

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
    fun fetchBackgroundImageForTitle(title: String): String? {

        val cleanName = Utils.cleanString(title)
        val language = Locale.getDefault().language
        val requestUrl = "$baseUrl/search/multi?api_key=$apiKey&page=1&language=$language&query=${java.net.URLEncoder.encode(cleanName, "UTF-8")}"

        val cleanUrl = cleanString(requestUrl);
        var response: String? = null
        apiCache?.let { cache ->
            if (cache.containsKey(cleanUrl)) {
                response = cache.get(cleanUrl)
                println("Found Cached Api Response for $title -> $response")
            }
        }

        if (response == null) {
            val searchResponse = client.newCall(Request.Builder().url(requestUrl).build()).execute()
            if (!searchResponse.isSuccessful) throw Exception("Failed to fetch search results: ${searchResponse.code}")
            response = searchResponse.body.string()
            apiCache?.put(cleanUrl, response)
        }

        val searchResult = gson.fromJson(response, SearchResult::class.java)

        // Select the best match
        val bestMatch = searchResult.results.firstOrNull() ?: return null

        // Fetch the backdrop image
        return bestMatch.backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }
    }

    enum class TimeWindow(val value: String) {
        DAY("day"),
        WEEK("week")
    }
    /**
     * Fetches a movie or TV show's background image URL based on the title with prioritized sorting.
     *
     * @param timeWindow The time window for which to fetch popular titles.
     * @return The URL of the background image or null if not found.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchBackgroundImagesForPopularTitles(timeWindow: TimeWindow): List<String> {
        // https://api.themoviedb.org/3/trending/all/day
        val language = Locale.getDefault().language
        val timeWindowParam = timeWindow.value
        val requestUrl = "$baseUrl/trending/all/$timeWindowParam?api_key=$apiKey&page=1&language=$language}"

        val cleanUrl = cleanString(requestUrl);
        var response: String? = null
        apiCache?.let { cache ->
            if (cache.containsKey(cleanUrl)) {
                response = cache.get(cleanUrl)
                println("Found Cached Api Response for Trending titles -> $response")
            }
        }
        if (response == null) {
            val apiResponse = client.newCall(Request.Builder().url(requestUrl).build()).execute()
            if (!apiResponse.isSuccessful) throw Exception("Failed to fetch search results: ${apiResponse.code}")
            response = apiResponse.body.string()
            apiCache?.put(cleanUrl, response)
        }
        val parsedResponse = gson.fromJson(response, SearchResult::class.java)

        return parsedResponse.results
            .mapNotNull { it.backdropPath }.map { "https://image.tmdb.org/t/p/original$it" }
    }

    // Data classes for parsing TMDb API response
    data class SearchResult(
        @SerializedName("results") val results: List<Result>
    )

    data class Result(
        @SerializedName("backdrop_path") val backdropPath: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("media_type") val mediaType: String?, // e.g., "movie" or "tv"
        @SerializedName("popularity") val popularity: Double?,
        @SerializedName("release_date") val releaseDate: String? // Format: YYYY-MM-DD
    )
}
