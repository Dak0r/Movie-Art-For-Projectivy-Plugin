package tv.projectivy.plugin.wallpaperprovider.cinemaglow.helpers

import android.content.Context
import android.net.Uri

class LottieEditorRegex(private val context: Context, private val fileUri: Uri) {
    private lateinit var jsonContent: String

    // Load the Lottie JSON file
    fun load(): LottieEditorRegex {
        val inputStream = context.contentResolver.openInputStream(fileUri)
            ?: throw IllegalArgumentException("Unable to open URI: $fileUri")
        jsonContent = inputStream.bufferedReader().use { it.readText() }
        return this
    }

    private fun intToLottieColor(color: Int): List<Float> {
        // val alpha = ((color shr 24) and 0xFF) / 255f // Extract alpha and normalize
        val red = ((color shr 16) and 0xFF) / 255f   // Extract red and normalize
        val green = ((color shr 8) and 0xFF) / 255f  // Extract green and normalize
        val blue = (color and 0xFF) / 255f           // Extract blue and normalize

        return listOf(red, green, blue) // RGB format
    }

    // Replace gradient colors and positions
    fun replaceGradientColors(positions: List<Float>, colors: List<Int>): LottieEditorRegex {
        if (positions.size != colors.size) {
            throw IllegalArgumentException("Positions and colors must have the same size.")
        }

        // Generate the replacement string for the gradient stops
        val gradientStops = mutableListOf<String>()
        for (i in positions.indices) {
            val color = colors[i]
            val rgba = intToLottieColor(color)
            gradientStops.add("${positions[i]}, ${rgba.joinToString(", ")}")
        }

        val gradientRegex = Regex("""("k": \{\s*"a": \d,\s*"k": \[[0-9.,\s]*?])""")

        // Replace the gradient stops for the matched shape
        jsonContent = gradientRegex.replace(jsonContent) { matchResult ->
            val original = matchResult.value
            original.replace(Regex("""("k":\s?)\[[0-9.,\s]*?]""")) {
                """${it.groupValues[1]}[${gradientStops.joinToString(", ")}]"""
            }
        }

        return this
    }

    // Save the updated Lottie JSON file
    fun save(outputUri: Uri): LottieEditorRegex {
        val outputStream = context.contentResolver.openOutputStream(outputUri)
            ?: throw IllegalArgumentException("Unable to open URI: $outputUri")
        outputStream.bufferedWriter().use { it.write(jsonContent) }
        println("Updated Lottie JSON saved to $outputUri")
        return this
    }
}
