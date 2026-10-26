package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileNamesTest {
    @Test
    fun entryFileNameUsesTimestampAndSanitizedTitle() {
        val entry = DiaryEntry(
            id = 1,
            title = "  工作/生活: 第一天?  \n",
            createdAt = 0,
            updatedAt = 0,
        )
        val now = Instant.parse("2026-10-26T06:30:00Z").toEpochMilli()

        val name = FileNames.forEntry(
            entry = entry,
            nowMillis = now,
            zoneId = ZoneId.of("Asia/Shanghai"),
        )

        assertEquals("2026-10-26_1430_工作_生活_ 第一天_.txt", name)
        assertFalse(name.contains('/'))
        assertFalse(name.contains(':'))
        assertFalse(name.contains('?'))
    }

    @Test
    fun blankTitleFallsBackToUntitled() {
        val entry = DiaryEntry(title = " \n ")
        assertTrue(FileNames.forEntry(entry).endsWith("_无标题.txt"))
    }

    @Test
    fun reservedWindowsNameIsMadeSafe() {
        assertEquals("_CON", FileNames.sanitizeTitle("CON"))
    }
}

