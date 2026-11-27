package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class FileCacheTest {

    @Test
    fun getCacheDirFallbackLogicWhenExternalIsNull() {
        val externalCacheDir: File? = null
        val internalCacheDir = File("/tmp/internalCache")

        val resultDir = externalCacheDir ?: internalCacheDir
        assertEquals(internalCacheDir, resultDir)
    }

    @Test
    fun getCacheDirReturnsExternalWhenAvailable() {
        var externalCacheDir: File? = File("/tmp/externalCache")
        val internalCacheDir = File("/tmp/internalCache")

        val resultDir = externalCacheDir ?: internalCacheDir
        assertEquals(File("/tmp/externalCache"), resultDir)

        externalCacheDir = null
        val fallbackDir = externalCacheDir ?: internalCacheDir
        assertEquals(internalCacheDir, fallbackDir)
    }
}
