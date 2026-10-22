package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppServiceRotationTest {

    @Test
    fun eachCallShowsTheNextTitleInOrder() {
        val rotation = AppServiceRotation()
        val titles = listOf("a", "b", "c")
        assertEquals("a", rotation.next("netflix", titles))
        assertEquals("b", rotation.next("netflix", titles))
        assertEquals("c", rotation.next("netflix", titles))
    }

    @Test
    fun wrapsAroundAfterTheLastTitle() {
        val rotation = AppServiceRotation()
        val titles = listOf("a", "b")
        rotation.next("netflix", titles)
        rotation.next("netflix", titles)
        assertEquals("a", rotation.next("netflix", titles))
    }

    @Test
    fun onlyTheFirstSizeTitlesAreUsed() {
        val rotation = AppServiceRotation(size = 2)
        val titles = listOf("a", "b", "c", "d")
        val shown = (1..4).map { rotation.next("netflix", titles) }
        assertEquals(listOf("a", "b", "a", "b"), shown)
    }

    @Test
    fun eachAppKeepsItsOwnPosition() {
        val rotation = AppServiceRotation()
        val titles = listOf("a", "b", "c")
        rotation.next("netflix", titles)
        rotation.next("netflix", titles)
        assertEquals("a", rotation.next("hulu", titles))
        assertEquals("c", rotation.next("netflix", titles))
    }

    @Test
    fun aShorterListStillGivesAValidTitle() {
        val rotation = AppServiceRotation()
        repeat(5) { rotation.next("netflix", listOf("a", "b", "c", "d", "e", "f")) }
        // Position 4 was shown last; the list shrank to 3, so the next one wraps to position (4 + 1) % 3
        assertEquals("c", rotation.next("netflix", listOf("a", "b", "c")))
    }

    @Test
    fun timerRotatesTheFocusedAppWhenEnabled() {
        val rotation = AppServiceRotation()
        rotation.focusedApp = "com.hulu.livingroomplus"
        assertEquals("com.hulu.livingroomplus", rotation.appToRotateOnTimer(rotationEnabled = true))
    }

    @Test
    fun timerDoesNothingForOtherFallbacks() {
        val rotation = AppServiceRotation()
        rotation.focusedApp = "com.hulu.livingroomplus"
        assertNull(rotation.appToRotateOnTimer(rotationEnabled = false))
    }

    @Test
    fun timerDoesNothingAfterAProgramCardOrBeforeAnyFocus() {
        val rotation = AppServiceRotation()
        assertNull(rotation.appToRotateOnTimer(rotationEnabled = true))
        rotation.focusedApp = "com.hulu.livingroomplus"
        rotation.focusedApp = null
        assertNull(rotation.appToRotateOnTimer(rotationEnabled = true))
    }

    @Test
    fun noTitlesGivesNull() {
        assertNull(AppServiceRotation().next("netflix", emptyList<String>()))
    }
}
