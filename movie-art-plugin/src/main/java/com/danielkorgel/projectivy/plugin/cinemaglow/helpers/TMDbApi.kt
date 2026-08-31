package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.Utils.cleanString
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.Locale

class TMDbApi(private val apiKey: String, private val apiCache: ApiResponseCache?) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    // Base URL for TMDb API
    private val baseUrl = "https://api.themoviedb.org/3"

    /**
     * Searches for a movie or TV show and returns its basic info (id, media type, backdrop URL).
     */
    fun searchArtInfo(title: String): SearchResultInfo? {
        val cleanName = cleanString(title)
        val language = Locale.getDefault().language
        val requestUrl = "$baseUrl/search/multi?api_key=$apiKey&page=1&language=$language&query=${java.net.URLEncoder.encode(cleanName, "UTF-8")}"

        val cleanUrl = cleanString(requestUrl)
        var response: String? = null
        apiCache?.let { cache ->
            if (cache.containsKey(cleanUrl)) {
                response = cache.get(cleanUrl)
            }
        }

        if (response == null) {
            val searchResponse = client.newCall(Request.Builder().url(requestUrl).build()).execute()
            if (!searchResponse.isSuccessful) return null
            response = searchResponse.body.string()
            apiCache?.put(cleanUrl, response, 604800) // Cache for 7 days
        }

        val searchResult = gson.fromJson(response, SearchResult::class.java)
        val bestMatch = searchResult.results.firstOrNull() ?: return null

        return SearchResultInfo(
            id = bestMatch.id,
            mediaType = bestMatch.mediaType ?: "movie",
            backdropUrl = bestMatch.backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }
        )
    }

    /**
     * Fetches the logo URL for a specific movie or TV show.
     */
    fun fetchLogoUrl(mediaType: String, id: Int): String? {
        val language = Locale.getDefault().language
        val requestUrl = "$baseUrl/$mediaType/$id/images?api_key=$apiKey&include_image_language=$language,en,null"

        val cleanUrl = cleanString(requestUrl)
        var response: String? = null
        apiCache?.let { cache ->
            if (cache.containsKey(cleanUrl)) {
                response = cache.get(cleanUrl)
            }
        }

        if (response == null) {
            val imageResponse = client.newCall(Request.Builder().url(requestUrl).build()).execute()
            if (!imageResponse.isSuccessful) return null
            response = imageResponse.body.string()
            apiCache?.put(cleanUrl, response, 604800)
        }

        val imageResult = gson.fromJson(response, ImageResult::class.java)
        val logo = imageResult.logos.firstOrNull { it.iso == language }
            ?: imageResult.logos.firstOrNull { it.iso == "en" }
            ?: imageResult.logos.firstOrNull()

        return logo?.filePath?.let { "https://image.tmdb.org/t/p/original$it" }
    }

    data class SearchResultInfo(
        val id: Int,
        val mediaType: String,
        val backdropUrl: String?,
    )

    @Suppress("unused")
    enum class TimeWindow(val value: String) {
        DAY("day"),
        WEEK("week")
    }
    /**
     * Fetches popular movie/TV show info.
     *
     * @param timeWindow The time window for which to fetch popular titles.
     * @return A list of SearchResultInfo objects.
     */
    fun fetchPopularTitlesInfo(timeWindow: TimeWindow): List<SearchResultInfo> {
        val language = Locale.getDefault().language
        val timeWindowParam = timeWindow.value
        val requestUrl = "$baseUrl/trending/all/$timeWindowParam?api_key=$apiKey&page=1&language=$language"

        val cleanUrl = cleanString(requestUrl)
        var response: String? = null
        apiCache?.let { cache ->
            if (cache.containsKey(cleanUrl)) {
                response = cache.get(cleanUrl)
            }
        }
        if (response == null) {
            val apiResponse = client.newCall(Request.Builder().url(requestUrl).build()).execute()
            if (!apiResponse.isSuccessful) return emptyList()
            response = apiResponse.body.string()
            apiCache?.put(cleanUrl, response)
        }
        val parsedResponse = gson.fromJson(response, SearchResult::class.java)

        return parsedResponse.results.map { result ->
            SearchResultInfo(
                id = result.id,
                mediaType = result.mediaType ?: "movie",
                backdropUrl = result.backdropPath?.let { "https://image.tmdb.org/t/p/original$it" }
            )
        }
    }

    // Data classes for parsing TMDb API response
    data class SearchResult(
        @SerializedName("results") val results: List<Result>
    )

    data class Result(
        @SerializedName("id") val id: Int,
        @SerializedName("backdrop_path") val backdropPath: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("media_type") val mediaType: String?, // e.g., "movie" or "tv"
        @SerializedName("popularity") val popularity: Double?,
        @SerializedName("release_date") val releaseDate: String? // Format: YYYY-MM-DD
    )

    data class ImageResult(
        @SerializedName("logos") val logos: List<Logo>
    )

    data class Logo(
        @SerializedName("file_path") val filePath: String?,
        @SerializedName("iso_639_1") val iso: String?
    )
}
