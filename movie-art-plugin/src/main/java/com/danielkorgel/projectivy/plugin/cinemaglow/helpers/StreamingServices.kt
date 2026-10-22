package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

/**
 * Maps Android TV app package names to TheMovieDB.org watch provider ids, so a focused app card can
 * show something popular on that app's own service.
 * Several ids are joined with "|", which TMDb's discover endpoint reads as OR (same service under
 * different ids, e.g. per plan). Ids were checked against TMDb's US provider list, and package names
 * against Google Play.
 * Provider ids: https://developer.themoviedb.org/reference/watch-providers-movie-list
 */
object StreamingServices {

    internal val providersByPackage = mapOf(
        // Subscription services
        "com.netflix.ninja" to "8|1796", // Netflix, Netflix Standard with Ads
        "com.amazon.amazonvideo.livingroom" to "9|119", // Prime Video
        "com.disney.disneyplus" to "337", // Disney+
        "com.hulu.livingroomplus" to "15", // Hulu
        "com.hulu.plus" to "15",
        "com.wbd.stream" to "1899", // HBO Max
        "com.apple.atve.androidtv.appletv" to "350", // Apple TV
        "com.apple.atve.amazon.appletv" to "350", // Apple TV (Fire TV)
        "com.peacocktv.peacockandroid" to "386|387", // Peacock
        "com.cbs.ott" to "531|2303|2616", // Paramount+ (US: Premium, Essential)
        "com.bydeluxe.d3.android.program.starz" to "43", // STARZ
        "com.epix.epix.now" to "34", // MGM+
        "com.amcplus.amcfullepisodes" to "526", // AMC+
        "com.discovery.discoveryplus.mobile" to "520", // discovery+
        "com.curiosity.curiositystream" to "190", // Curiosity Stream

        // Anime, international and niche
        "com.crunchyroll.crunchyroid" to "283", // Crunchyroll
        "com.hidive.android" to "430", // HIDIVE
        "com.viki.android" to "344", // Rakuten Viki
        "com.britbox.us" to "151", // BritBox
        "com.britbox.tv" to "151",
        "com.acorn.tv" to "87", // Acorn TV
        "com.dramafever.shudder" to "99", // Shudder
        "com.mubi" to "11", // MUBI
        "com.criterionchannel" to "258", // The Criterion Channel

        // Free with ads, or through a library
        "com.tubitv" to "73", // Tubi
        "tv.pluto.android" to "300", // Pluto TV
        "com.roku.web.trc" to "207", // The Roku Channel
        "com.pbs.video" to "209", // PBS
        "com.kanopy" to "191", // Kanopy
        "com.hoopladigital.android" to "212", // Hoopla

        // Live TV services (their on-demand catalog)
        "com.google.android.youtube.tvunplugged" to "2528", // YouTube TV
        "com.fubo.firetv.screen" to "257", // Fubo
        "com.philo.philo.google" to "2383", // Philo
    )

    /** Watch provider ids for the given app, or null if the app isn't a known streaming service. */
    fun providerIdsFor(packageName: String?): String? = packageName?.let { providersByPackage[it] }
}
