package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import android.graphics.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale

object ImageProcessor {
    /**
     * Composites a movie logo onto a backdrop and adds a gradient shadow at the bottom.
     */
    fun processMovieArt(
        backdrop: Bitmap,
        logo: Bitmap?,
        addShadow: Boolean = true,
    ): Bitmap {
        val width = backdrop.width
        val height = backdrop.height

        val resultBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 1. Draw backdrop
        canvas.drawBitmap(backdrop, 0f, 0f, null)

        // 2. Add gradient shadow at the bottom
        // This helps readability of UI elements placed at the bottom
        if (addShadow) {
            val isBright = isBottomPartBright(backdrop)
            
            val shadowHeight: Float
            val midAlpha: Int
            val maxAlpha: Int

            if (isBright) {
                // Current version: Stronger shadow, shorter height
                shadowHeight = height * 0.33f
                midAlpha = 180
                maxAlpha = 255
            } else {
                // Previous version: Softer shadow, taller height
                shadowHeight = height * 0.5f
                midAlpha = 150
                maxAlpha = 230
            }

            val shadowPaint = Paint().apply {
                shader = LinearGradient(
                    0f, height - shadowHeight,
                    0f, height.toFloat(),
                    intArrayOf(
                        Color.argb(0, 0, 0, 0),
                        Color.argb(midAlpha, 0, 0, 0),
                        Color.argb(maxAlpha, 0, 0, 0)
                    ),
                    floatArrayOf(0f, 0.4f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, height - shadowHeight, width.toFloat(), height.toFloat(), shadowPaint)
        }

        // 3. Composite Logo
        logo?.let { l ->
            // Scale logo to a reasonable size (e.g., max 15% of width or 12% of height)
            // Reduced even further to ensure they feel smaller (was 20%/17%)
            val maxLogoWidth = width * 0.15f
            val maxLogoHeight = height * 0.12f
            
            val scale = minOf(maxLogoWidth / l.width, maxLogoHeight / l.height)
            val logoW = (l.width * scale).toInt()
            val logoH = (l.height * scale).toInt()

            val scaledLogo = l.scale(logoW, logoH, true)

            // Placement: Top Left with 5% margin
            val marginX = width * 0.05f
            val marginY = height * 0.05f

            // Add a basic blob shadow (radial gradient) behind the logo
            // This is much faster than BlurMaskFilter and provides a soft glow
            val centerX = marginX + (logoW / 2f)
            val centerY = marginY + (logoH / 2f)
            val shadowRadius = maxOf(logoW.toFloat(), logoH.toFloat()) * 0.8f

            val blobPaint = Paint().apply {
                isAntiAlias = true
                shader = RadialGradient(
                    centerX, centerY, shadowRadius,
                    intArrayOf(Color.argb(140, 0, 0, 0), Color.TRANSPARENT),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(centerX, centerY, shadowRadius, blobPaint)

            val logoPaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
            }
            canvas.drawBitmap(scaledLogo, marginX, marginY, logoPaint)
        }

        return resultBitmap
    }

    private fun isBottomPartBright(bitmap: Bitmap): Boolean {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val startY = height - (height / 3)
            val step = 20 // sample every 20 pixels
            var totalLuminance = 0.0
            var count = 0
            
            for (x in 0 until width step step) {
                for (y in startY until height step step) {
                    val color = bitmap[x, y]
                    val r = Color.red(color)
                    val g = Color.green(color)
                    val b = Color.blue(color)
                    totalLuminance += (0.299 * r) + (0.587 * g) + (0.114 * b)
                    count++
                }
            }
            
            if (count == 0) return true
            (totalLuminance / count) > 150 // Threshold for "bright"
        } catch (_: Exception) {
            true // Default to bright (stronger shadow) on error
        }
    }
}
