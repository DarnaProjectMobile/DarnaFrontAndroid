package com.sim.darna.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private const val DISPLAY_FORMAT = "yyyy/MM/dd"
    // Handle typical ISO 8601 formats from backend
    private val inputFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd"
    )

    fun formatToYYYYMMDD(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        // If it already looks like yyyy/MM/dd, just return it
        if (dateString.matches(Regex("\\d{4}/\\d{2}/\\d{2}"))) {
            return dateString
        }

        for (format in inputFormats) {
            try {
                val inputSdf = SimpleDateFormat(format, Locale.getDefault())
                val date = inputSdf.parse(dateString)
                if (date != null) {
                    val outputSdf = SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault())
                    return outputSdf.format(date)
                }
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        // If parsing fails, return original string (best effort)
        return dateString
    }
}
