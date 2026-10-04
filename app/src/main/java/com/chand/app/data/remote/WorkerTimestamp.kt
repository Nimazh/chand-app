package com.chand.app.data.remote

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Cloudflare's JavaScript Date.toISOString() timestamp, without an API 26 java.time dependency. */
internal fun parseWorkerTimestamp(value: String): Long {
    for (pattern in listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")) {
        val formatter = SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val position = ParsePosition(0)
        val date = formatter.parse(value, position)
        if (date != null && position.index == value.length) return date.time
    }
    throw IllegalArgumentException("Invalid Worker UTC timestamp")
}
