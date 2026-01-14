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

    // Preference keys for custom app background
    const val APP_BACKGROUND = "app_background"
    const val KEY_CUSTOM_APP_BACKGROUND_NAME = "custom_app_background_path"

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Custom app background helpers
    var useCustomAppBackground: Boolean
        get() = get(APP_BACKGROUND, "DynamicColors") == "CustomBackground"
        set(value) = set(APP_BACKGROUND, if (value) "CustomBackground" else "DynamicColors")

    var customAppBackgroundName: String?
        get() {
            val path: String = get(KEY_CUSTOM_APP_BACKGROUND_NAME, "")
            return if (path.isEmpty()) null else path
        }
        set(value) = set(KEY_CUSTOM_APP_BACKGROUND_NAME, value ?: "")

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun set(key: String, value: Any?) =
        when (value) {
            is String? -> preferences.edit { it.putString(key, value) }
            is Int -> preferences.edit { it.putInt(key, value) }
            is Boolean -> preferences.edit { it.putBoolean(key, value) }
            is Float -> preferences.edit { it.putFloat(key, value) }
            is Long -> preferences.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
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
            Long::class -> preferences.getLong(key, defaultValue as? Long ?: -1) as T
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
                    is Long -> editor.putInt(key, value.toInt())
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
