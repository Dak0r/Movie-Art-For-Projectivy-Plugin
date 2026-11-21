package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import com.danielkorgel.projectivy.plugin.cinemaglow.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamingServicesTest {

    @Test
    fun knownAppsMapToTheirProviderIds() {
        assertEquals("8|1796", StreamingServices.providerIdsFor("com.netflix.ninja"))
        assertEquals("15", StreamingServices.providerIdsFor("com.hulu.livingroomplus"))
        assertEquals("1899", StreamingServices.providerIdsFor("com.wbd.stream"))
    }

    @Test
    fun unknownOrMissingAppsHaveNoProvider() {
        assertNull(StreamingServices.providerIdsFor("com.unknown.app"))
        assertNull(StreamingServices.providerIdsFor(null))
    }

    @Test
    fun providerIdsAreNumbersJoinedWithPipes() {
        StreamingServices.providersByPackage.forEach { (pkg, ids) ->
            assertTrue("$pkg -> $ids", Regex("""\d+(\|\d+)*""").matches(ids))
        }
    }

    @Test
    fun packageNamesLookLikePackageNames() {
        StreamingServices.providersByPackage.keys.forEach { pkg ->
            assertTrue(pkg, Regex("""[a-z][a-z0-9_]*(\.[a-z0-9_]+)+""").matches(pkg))
        }
    }

    @Test
    fun allDefinedProviderIdsAreValidOnTmdbWhenKeyPresent() {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("TMDB_API_KEY")
            field.get(null) as? String
        } catch (_: Throwable) {
            System.getenv("TMDB_API_KEY")
        }

        if (apiKey.isNullOrEmpty() || apiKey == "null") {
            println("Skipping TMDb provider validation: TMDB_API_KEY is not configured.")
            return
        }

        val gson = com.google.gson.Gson()
        val validProviderIds = mutableSetOf<Int>()

        listOf("movie", "tv").forEach { type ->
            try {
                val url = java.net.URI("https://api.themoviedb.org/3/watch/providers/$type?api_key=$apiKey").toURL()
                val json = url.readText()
                val parsed = gson.fromJson(json, ProviderListResponse::class.java)
                parsed?.results?.map { it.providerId }?.let { validProviderIds.addAll(it) }
            } catch (e: Exception) {
                println("Failed to fetch $type watch providers: ${e.message}")
            }
        }

        if (validProviderIds.isEmpty()) {
            println("Warning: TMDb API provider list returned empty. Skipping strict check.")
            return
        }

        StreamingServices.providersByPackage.forEach { (pkg, idsStr) ->
            val ids = idsStr.split("|").mapNotNull { it.toIntOrNull() }
            val hasValidProvider = ids.any { id -> id in validProviderIds }
            assertTrue("Package '$pkg' specifies provider IDs '$idsStr', none of which were found in TMDb's watch provider directory", hasValidProvider)
        }
    }

    @Test
    fun verifyArdZdfAndJoynReturnTitlesFromTmdb() {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("TMDB_API_KEY")
            field.get(null) as? String
        } catch (_: Throwable) {
            System.getenv("TMDB_API_KEY")
        } ?: return

        val tmdbApi = TMDbApi(apiKey, null)

        val GermanApps = listOf(
            "de.prosiebensat1.joyn",
            "de.zdf.android.mediathek",
            "de.swr.avp.ard.tv",
            "uk.co.bbc.iplayer"
        )

        GermanApps.forEach { pkg ->
            val providerIds = StreamingServices.providerIdsFor(pkg)
            assertTrue("Package $pkg should be mapped in StreamingServices", providerIds != null)
            val titles = tmdbApi.fetchPopularOnProvidersInfo(providerIds!!, "DE")
            println("App '$pkg' (providerIds=$providerIds) returned ${titles.size} titles in DE")
            assertTrue("App '$pkg' (providerIds=$providerIds) should return non-empty titles from TMDb in DE", titles.isNotEmpty())
        }
    }

    @Test
    fun verifyAllMappedBundleIdentifiersReturnTitlesFromTmdb() {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("TMDB_API_KEY")
            field.get(null) as? String
        } catch (_: Throwable) {
            System.getenv("TMDB_API_KEY")
        }

        if (apiKey.isNullOrEmpty() || apiKey == "null") {
            println("Skipping TMDb bundle identifier verification: TMDB_API_KEY is not configured.")
            return
        }

        val tmdbApi = TMDbApi(apiKey, null)
        val emptyPackages = mutableListOf<String>()

        StreamingServices.providersByPackage.forEach { (pkg, providerIds) ->
            val region = when {
                pkg.startsWith("de.") || pkg.contains("zdf") || pkg.contains("sky") || pkg.contains("swr") -> "DE"
                pkg.startsWith("uk.") || pkg.contains("bbc") || pkg.startsWith("air.itv") -> "GB"
                pkg.startsWith("fr.") || pkg.contains("francetv") -> "FR"
                pkg.startsWith("es.") || pkg.contains("movistar") || pkg.contains("rtve") -> "ES"
                pkg.startsWith("it.") || pkg.contains("rai") -> "IT"
                pkg.startsWith("nl.") -> "NL"
                pkg.startsWith("no.") -> "NO"
                pkg.startsWith("se.") -> "SE"
                pkg.startsWith("dk.") -> "DK"
                else -> "US"
            }

            val titles = tmdbApi.fetchPopularOnProvidersInfo(providerIds, region)
            if (titles.isEmpty()) {
                emptyPackages.add("$pkg (providerIds=$providerIds, region=$region)")
            }
        }

        if (emptyPackages.isNotEmpty()) {
            println("Packages returning 0 titles from TMDb: $emptyPackages")
        }
        assertTrue("All mapped bundle identifiers should return titles from TMDb. Empty packages: $emptyPackages", emptyPackages.isEmpty())
    }

    private data class ProviderListResponse(
        @com.google.gson.annotations.SerializedName("results") val results: List<ProviderItem>? = null
    )

    private data class ProviderItem(
        @com.google.gson.annotations.SerializedName("provider_id") val providerId: Int
    )
}
