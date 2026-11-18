package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

/**
 * Maps Android TV app package names to TheMovieDB.org watch provider ids, so a focused app card can
 * show something popular on that app's own service.
 * Several ids are joined with "|", which TMDb's discover endpoint reads as OR (same service under
 * different ids, e.g. per plan). Ids were checked against TMDb's provider list across US, UK, DE,
 * FR, ES, IT, NL, and Nordics, and package names against Google Play.
 * Provider ids: https://developer.themoviedb.org/reference/watch-providers-movie-list
 */
object StreamingServices {

    internal val providersByPackage = mapOf(
        // Global & US Subscription Services
        "com.netflix.ninja" to "8|1796", // Netflix, Netflix Standard with Ads
        "com.amazon.amazonvideo.livingroom" to "9|119", // Prime Video
        "com.disney.disneyplus" to "337", // Disney+
        "com.hulu.livingroomplus" to "15", // Hulu
        "com.hulu.plus" to "15",
        "com.wbd.stream" to "1899", // Max / HBO Max
        "com.hbo.hbonow" to "1899|384", // HBO Max legacy
        "com.apple.atve.androidtv.appletv" to "350", // Apple TV
        "com.apple.atve.amazon.appletv" to "350", // Apple TV (Fire TV)
        "com.peacocktv.peacockandroid" to "386|387", // Peacock
        "com.cbs.ott" to "531|2303|2616", // Paramount+
        "com.bydeluxe.d3.android.program.starz" to "43", // STARZ
        "com.epix.epix.now" to "34", // MGM+
        "com.amcplus.amcfullepisodes" to "526", // AMC+
        "com.discovery.discoveryplus.mobile" to "520", // discovery+
        "com.curiosity.curiositystream" to "190", // Curiosity Stream
        "com.plexapp.android" to "538", // Plex Movies & TV

        // Germany (DE), Austria (AT) & Switzerland (CH)
        "de.sky.online" to "30", // WOW (Sky DE)
        "de.sky.ticket" to "30",
        "de.rtli.tvnow" to "178", // RTL+
        "de.rtli.tvnow.androidtv" to "178",
        "de.prosiebensat1.joyn" to "304|421", // Joyn, Joyn Plus
        "de.prosiebensat1digital.seventv" to "304|421", // Joyn, Joyn Plus
        "de.zdf.android.mediathek" to "537", // ZDF
        "com.zdf.android.mediathek" to "537", // ZDF
        "de.zdf.mediathek" to "537", // ZDF
        "de.daserste.mediathek" to "219|1902", // ARD Mediathek, ARD Plus
        "de.swr.avp.ard.tv" to "219|1902", // ARD Mediathek, ARD Plus
        "de.ard.mediathek" to "219|1902", // ARD Mediathek, ARD Plus

        // United Kingdom (UK)
        "uk.co.bbc.iplayer" to "38|329|338|285", // BBC iPlayer / Player
        "uk.co.itv.itvhub.tve" to "41|1771", // ITVX
        "air.itv.mobile.app" to "41|1771",

        // France (FR)
        "fr.tf1.mytf1" to "559", // TF1+

        // Spain (ES)
        "es.rtve.rtveplay" to "541", // RTVE Play
        "es.rtve.alacarta" to "541",

        // Italy (IT)
        "it.rai.play" to "222", // RaiPlay
        "it.mediaset.video" to "359", // Mediaset Infinity

        // Netherlands (NL) & Nordics (SE, NO, DK)
        "nl.videoland.android" to "71", // Videoland (NL)

        // Anime, International & Niche
        "com.crunchyroll.crunchyroid" to "283", // Crunchyroll
        "com.hidive.android" to "430", // HIDIVE
        "com.viki.android" to "344", // Rakuten Viki
        "com.britbox.us" to "151", // BritBox
        "com.britbox.tv" to "151",
        "com.acorn.tv" to "87", // Acorn TV
        "com.dramafever.shudder" to "99", // Shudder
        "com.mubi" to "11", // MUBI
        "com.criterionchannel" to "258", // The Criterion Channel

        // Free with ads / FAST services
        "com.tubitv" to "73", // Tubi
        "tv.pluto.android" to "300", // Pluto TV
        "com.roku.web.trc" to "207", // The Roku Channel
        "com.amazon.imdb.tv.android.app" to "613", // Amazon Freevee
        "com.pbs.video" to "209", // PBS
        "com.kanopy" to "191", // Kanopy
        "com.hoopladigital.android" to "212", // Hoopla

        // Live TV services (their on-demand catalog)
        "com.google.android.youtube.tvunplugged" to "2528", // YouTube TV
        "com.fubo.firetv.screen" to "257", // Fubo
        "com.philo.philo.google" to "2383", // Philo
        "com.sling" to "299", // Sling TV
        "com.att.tv" to "444", // DirecTV Stream
    )

    /** Watch provider ids for the given app, or null if the app isn't a known streaming service. */
    fun providerIdsFor(packageName: String?): String? = packageName?.let { providersByPackage[it] }
}
