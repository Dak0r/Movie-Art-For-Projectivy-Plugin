package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

/**
 * Walks each app's titles in order, so every visit to an app shows the next of its most popular
 * titles instead of a random one. Positions are kept per app and in memory only.
 */
class AppServiceRotation(private val size: Int = 10) {

    private val positions = mutableMapOf<String, Int>()

    /** The next of the first [size] titles for this app (wrapping around), or null if there are none. */
    fun <T> next(packageName: String, titles: List<T>): T? {
        val top = titles.take(size)
        if (top.isEmpty()) return null
        val position = ((positions[packageName] ?: -1) + 1) % top.size
        positions[packageName] = position
        return top[position]
    }
}
