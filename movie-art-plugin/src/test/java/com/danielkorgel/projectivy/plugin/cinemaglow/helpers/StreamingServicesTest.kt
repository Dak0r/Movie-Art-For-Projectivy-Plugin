package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

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
        assertNull(StreamingServices.providerIdsFor("com.plexapp.android"))
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
}
