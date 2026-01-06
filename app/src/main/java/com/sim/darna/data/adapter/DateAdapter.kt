package com.sim.darna.data.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter : JsonAdapter<Date>() {
    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    override fun fromJson(reader: JsonReader): Date? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        
        val dateString = reader.nextString()
        if (dateString == null) return null
        
        // Try each format until one succeeds
        for (format in dateFormats) {
            try {
                return format.parse(dateString)
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        
        // If all formats fail, return null
        return null
    }

    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(dateFormats[0].format(value))
        }
    }
}

