package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object FileNames {
    private val invalidFileNameChars = Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]")
    private val repeatedWhitespace = Regex("\\s+")
    private val reservedWindowsNames = setOf(
        "CON",
        "PRN",
        "AUX",
        "NUL",
        "COM1",
        "COM2",
        "COM3",
        "COM4",
        "COM5",
        "COM6",
        "COM7",
        "COM8",
        "COM9",
        "LPT1",
        "LPT2",
        "LPT3",
        "LPT4",
        "LPT5",
        "LPT6",
        "LPT7",
        "LPT8",
        "LPT9",
    )
    private val fileNameTimeFormatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd_HHmm",
        Locale.US,
    )

    fun forEntry(
        entry: DiaryEntry,
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = "${timestampPrefix(nowMillis, zoneId)}_${sanitizeTitle(entry.title)}.txt"

    fun allEntriesZip(
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = "${timestampPrefix(nowMillis, zoneId)}_简日记.zip"

    fun backupZip(
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String = "${timestampPrefix(nowMillis, zoneId)}_简日记备份.zip"

    fun sanitizeTitle(rawTitle: String): String {
        var cleaned = rawTitle
            .trim()
            .replace(invalidFileNameChars, "_")
            .replace(repeatedWhitespace, " ")
            .trim(' ', '.')
            .take(60)
            .trim(' ', '.')

        if (cleaned.isBlank()) cleaned = "无标题"
        if (cleaned.substringBefore('.').uppercase(Locale.ROOT) in reservedWindowsNames) {
            cleaned = "_$cleaned"
        }
        return cleaned
    }

    private fun timestampPrefix(nowMillis: Long, zoneId: ZoneId): String =
        fileNameTimeFormatter.format(Instant.ofEpochMilli(nowMillis).atZone(zoneId))
}

