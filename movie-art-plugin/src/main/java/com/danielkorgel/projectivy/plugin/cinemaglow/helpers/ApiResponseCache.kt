package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.content.Context
import android.net.Uri
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class CacheEntry(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String,
    @SerializedName("expiresAt") val expiresAt: Long? = null
)

class ApiResponseCache(private val context: Context, private val fileUri: Uri) {

    private val cache: MutableMap<String, CacheEntry> = ConcurrentHashMap()

    init {
        loadCacheFromFile()
    }

    /**
     * Puts a value in the cache.
     * @param validForSeconds How long the entry is valid. If null, it defaults to midnight (0 AM) of the next day.
     */
    fun put(key: String, value: String, validForSeconds: Int? = null) {
        val expiresAt = if (validForSeconds != null) {
            System.currentTimeMillis() + (validForSeconds * 1000L)
        } else {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        cache[key] = CacheEntry(key, value, expiresAt)
        saveCacheToFile()
    }

    fun get(key: String): String? {
        val entry = cache[key] ?: return null
        
        // If expiresAt is missing (legacy) or expired, remove and return null
        if (entry.expiresAt == null || System.currentTimeMillis() > entry.expiresAt) {
            remove(key)
            return null
        }
        
        return entry.value
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
        val entry = cache[key] ?: return false
        if (entry.expiresAt == null || System.currentTimeMillis() > entry.expiresAt) {
            remove(key)
            return false
        }
        return true
    }

    private fun loadCacheFromFile() {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)
            inputStream?.use {
                val json = it.bufferedReader().readText()
                val entries = Json.decodeFromString<List<CacheEntry>>(json)
                val now = System.currentTimeMillis()
                
                // Only load entries that have an expiration date AND haven't expired yet
                val validEntries = entries.filter { entry ->
                    entry.expiresAt != null && entry.expiresAt > now
                }
                
                cache.clear()
                validEntries.forEach { entry ->
                    cache[entry.key] = entry
                }
            } ?: println("Cache file not found, initializing empty cache.")
        } catch (e: Exception) {
            println("Error loading cache: ${e.message}")
        }
    }

    private fun saveCacheToFile() {
        try {
            val contentResolver = context.contentResolver
            val entries = cache.values.toList()
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
