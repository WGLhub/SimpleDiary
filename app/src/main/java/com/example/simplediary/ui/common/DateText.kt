package com.example.simplediary.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd HH:mm",
    Locale.CHINA,
)

fun formatDateTime(timestamp: Long): String =
    displayFormatter.format(
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()),
    )

fun formatTime(timestamp: Long): String =
    DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
        .format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))
