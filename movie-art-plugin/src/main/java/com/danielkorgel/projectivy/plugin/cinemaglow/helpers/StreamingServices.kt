package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

/**
 * Maps Android TV app package names to TheMovieDB.org watch provider ids, so a focused app card can
 * show something popular on that app's own service.
 * Several ids are joined with "|", which TMDb's discover endpoint reads as OR (same service under
 * different ids, e.g. per plan). Ids were checked against TMDb's US provider list.
 * Provider ids: https://developer.themoviedb.org/reference/watch-providers-movie-list
 */
object StreamingServices {

    private val providersByPackage = mapOf(
        "com.netflix.ninja" to "8|1796", // Netflix, Netflix Standard with Ads
        "com.amazon.amazonvideo.livingroom" to "9|119", // Prime Video
        "com.disney.disneyplus" to "337", // Disney+
        "com.hulu.livingroomplus" to "15", // Hulu
        "com.hulu.plus" to "15",
        "com.wbd.stream" to "1899", // HBO Max
        "com.hbo.hbonow" to "1899",
        "com.apple.atve.androidtv.appletv" to "350", // Apple TV
        "com.apple.atve.amazon.appletv" to "350",
        "com.peacocktv.peacockandroid" to "386|387", // Peacock
        "com.cbs.ott" to "531|2303|2616", // Paramount+ (US: Premium, Essential)
        "com.crunchyroll.crunchyroid" to "283", // Crunchyroll
        "com.tubitv" to "73", // Tubi
    )

    /** Watch provider ids for the given app, or null if the app isn't a known streaming service. */
    fun providerIdsFor(packageName: String?): String? = packageName?.let { providersByPackage[it] }
}
