package com.danielkorgel.projectivy.plugin.cinemaglow

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken

object PreferencesManager {
    lateinit var preferences: SharedPreferences

    enum class FallbackBackground(val text: String) {
        PopularMoviesAndShows("Popular Movies and Shows"),
        DynamicColors("Dynamic Colors"),
        CustomBackground("Custom Background");
    }

    // Preference keys
    const val APP_BACKGROUND = "app_background"
    const val FALLBACK_BACKGROUND = "fallback_background"
    const val KEY_CUSTOM_APP_BACKGROUND_NAME = "custom_app_background_path"
    const val KEY_LAST_WALLPAPER = "last_wallpaper"
    const val KEY_LAST_CALLING_PID = "last_calling_pid"
    const val KEY_VIDEO_FORCED_UPDATE_COUNT = "video_forced_update_count"

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        // Migrate legacy setting
        val legacySetting = get(APP_BACKGROUND, "")
        if(legacySetting == FallbackBackground.CustomBackground.name) {
            set(FALLBACK_BACKGROUND, FallbackBackground.CustomBackground.name)
            set(APP_BACKGROUND, "")
        }
    }

    var fallbackBackground: FallbackBackground
        get() = FallbackBackground.valueOf(get(FALLBACK_BACKGROUND, FallbackBackground.PopularMoviesAndShows.name))
        set(value) = set(FALLBACK_BACKGROUND, value.name)


    var customAppBackgroundName: String?
        get() {
            val path: String = get(KEY_CUSTOM_APP_BACKGROUND_NAME, "")
            return path.ifEmpty { null }
        }
        set(value) = set(KEY_CUSTOM_APP_BACKGROUND_NAME, value ?: "")

    var lastWallpaper: String
        get() = get(KEY_LAST_WALLPAPER, "")
        set(value) = set(KEY_LAST_WALLPAPER, value)

    var lastCallingPid: Int
        get() = get(KEY_LAST_CALLING_PID, -1)
        set(value) = set(KEY_LAST_CALLING_PID, value)

    var videoForcedUpdateCount: Int
        get() = get(KEY_VIDEO_FORCED_UPDATE_COUNT, 2)
        set(value) = set(KEY_VIDEO_FORCED_UPDATE_COUNT, value)

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) =
        when (value) {
            is String -> preferences.edit { it.putString(key, value) }
            is Int -> preferences.edit { it.putInt(key, value) }
            is Boolean -> preferences.edit { it.putBoolean(key, value) }
            is Float -> preferences.edit { it.putFloat(key, value) }
            is Long -> preferences.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented: ${value?.javaClass?.simpleName}")
        }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T =
        when (T::class) {
            String::class -> preferences.getString(key, defaultValue as String? ?: "") as T
            Int::class -> preferences.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1L) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    fun export(): String {
        return Gson().toJson(preferences.all)
    }

    fun import(prefs: String): Boolean {
        val gson = GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map = gson.fromJson<Map<String, Any>>(prefs, type)
            val editor = preferences.edit()
            editor.clear()
            map.forEach { (key: String, value: Any) ->
                when(value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is String -> editor.putString(key, value)
                    is ArrayList<*> -> editor.putStringSet(key, java.util.HashSet(value as java.util.ArrayList<String>))
                    is Set<*> -> editor.putStringSet(key, value as Set<String>)
                }
            }
            editor.apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        return true
    }
}
