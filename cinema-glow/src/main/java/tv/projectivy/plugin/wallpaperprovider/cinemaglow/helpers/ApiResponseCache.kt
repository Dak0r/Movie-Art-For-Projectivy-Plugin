package tv.projectivy.plugin.wallpaperprovider.cinemaglow.helpers

import android.content.Context
import android.net.Uri
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class CacheEntry(val key: String, val value: String)

class ApiResponseCache(private val context: Context, private val fileUri: Uri) {

    private val cache: MutableMap<String, String> = ConcurrentHashMap()

    init {
        loadCacheFromFile()
    }

    fun put(key: String, value: String) {
        cache[key] = value
        saveCacheToFile()
    }

    fun get(key: String): String? {
        return cache[key]
    }

    fun remove(key: String) {
        cache.remove(key)
        saveCacheToFile()
    }

    fun clear() {
        cache.clear()
        saveCacheToFile()
    }

    fun containsKey(key: String): Boolean {
        return cache.containsKey(key)
    }

    private fun loadCacheFromFile() {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)
            inputStream?.use {
                val json = it.bufferedReader().readText()
                val entries = Json.decodeFromString<List<CacheEntry>>(json)
                cache.putAll(entries.associate { entry -> entry.key to entry.value })
            } ?: println("Cache file not found, initializing empty cache.")
        } catch (e: Exception) {
            println("Error loading cache: ${e.message}")
        }
    }

    private fun saveCacheToFile() {
        try {
            val contentResolver = context.contentResolver
            val entries = cache.map { CacheEntry(it.key, it.value) }
            val json = Json.encodeToString(entries)
            val outputStream = contentResolver.openOutputStream(fileUri)
            outputStream?.use {
                it.write(json.toByteArray())
            }
        } catch (e: Exception) {
            println("Error saving cache: ${e.message}")
        }
    }
}
