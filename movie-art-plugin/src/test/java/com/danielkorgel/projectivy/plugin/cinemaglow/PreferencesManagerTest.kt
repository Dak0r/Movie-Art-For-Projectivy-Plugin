package com.danielkorgel.projectivy.plugin.cinemaglow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PreferencesManagerTest {

    @Test
    fun preferencesManagerKeysAreDefined() {
        assertNotNull(PreferencesManager.KEY_APP_BACKGROUND)
        assertNotNull(PreferencesManager.KEY_FALLBACK_BACKGROUND)
        assertNotNull(PreferencesManager.KEY_LAST_WALLPAPER)
        assertNotNull(PreferencesManager.KEY_CUSTOM_APP_BACKGROUND_NAME)
        assertNotNull(PreferencesManager.KEY_CUSTOM_FALLBACK_BACKGROUND_NAME)
        assertNotNull(PreferencesManager.KEY_LAST_CALLING_PID)
        assertNotNull(PreferencesManager.KEY_VIDEO_FORCED_UPDATE_COUNT)
        assertNotNull(PreferencesManager.KEY_LAST_CACHE_CLEAN)
    }

    @Test
    fun fallbackBackgroundEnumValues() {
        val entries = PreferencesManager.FallbackBackground.entries
        assertEquals(3, entries.size)
        assertEquals("Popular Movies and Shows", PreferencesManager.FallbackBackground.PopularMoviesAndShows.text)
        assertEquals("Dynamic Colors", PreferencesManager.FallbackBackground.DynamicColors.text)
        assertEquals("Custom Background", PreferencesManager.FallbackBackground.CustomBackground.text)
    }
}
