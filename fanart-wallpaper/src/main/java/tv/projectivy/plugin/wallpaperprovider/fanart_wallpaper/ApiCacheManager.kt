import android.content.Context
import android.net.Uri
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class CacheEntry(val key: String, val value: String)

class ApiCacheManager(private val context: Context, private val fileUri: Uri) {

    private val cache: MutableMap<String, String> = ConcurrentHashMap()

    init {
        loadCacheFromFile()
    }

    // Stores an entry in the cache and persists the changes
    fun put(key: String, value: String) {
        cache[key] = value
        saveCacheToFile()
    }

    // Retrieves an entry from the cache
    fun get(key: String): String? {
        return cache[key]
    }

    // Removes an entry from the cache and persists the changes
    fun remove(key: String) {
        cache.remove(key)
        saveCacheToFile()
    }

    // Clears all entries from the cache and persists the changes
    fun clear() {
        cache.clear()
        saveCacheToFile()
    }

    // Checks if a key exists in the cache
    fun containsKey(key: String): Boolean {
        return cache.containsKey(key)
    }

    // Loads the cache from the file
    private fun loadCacheFromFile() {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)
            inputStream?.use {
                val json = it.bufferedReader().readText()
                val entries = Json.decodeFromString<List<CacheEntry>>(json)
                cache.putAll(entries.associate { it.key to it.value })
            } ?: println("Cache file not found, initializing empty cache.")
        } catch (e: Exception) {
            println("Error loading cache: ${e.message}")
        }
    }

    // Saves the cache to the file
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
