package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertNull(StreamingServices.providerIdsFor("com.plexapp.android"))
        assertNull(StreamingServices.providerIdsFor(null))
    }

    @Test
    fun providerIdsAreNumbersJoinedWithPipes() {
        val packages = listOf(
            "com.netflix.ninja", "com.amazon.amazonvideo.livingroom", "com.disney.disneyplus",
            "com.hulu.livingroomplus", "com.wbd.stream", "com.apple.atve.androidtv.appletv",
            "com.peacocktv.peacockandroid", "com.cbs.ott", "com.crunchyroll.crunchyroid", "com.tubitv",
        )
        packages.forEach { pkg ->
            val ids = StreamingServices.providerIdsFor(pkg)
            assert(ids != null && Regex("""\d+(\|\d+)*""").matches(ids)) { "$pkg -> $ids" }
        }
    }
}
