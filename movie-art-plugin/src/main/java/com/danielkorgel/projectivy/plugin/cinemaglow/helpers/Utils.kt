package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

object Utils {
    fun cleanString(input: String): String {
        // Remove content within square brackets (including the brackets)
        val noBrackets = input.replace("\\[.*?]".toRegex(), " ")
        // Replace all special characters with spaces
        val noSpecialChars = java.net.URLEncoder.encode(noBrackets, "utf-8")
        // Replace multiple sequential spaces with a single space
        return noSpecialChars.replace("\\s+".toRegex(), " ").trim()
    }
}