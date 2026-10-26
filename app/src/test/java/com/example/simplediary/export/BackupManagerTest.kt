package com.example.simplediary.export

import com.example.simplediary.data.local.ContentFormat
import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.data.settings.ThemeMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {
    private val manager = BackupManager()

    @Test
    fun backupRoundTripPreservesAllFieldsAndSettings() {
        val entries = listOf(
            DiaryEntry(
                id = 42,
                title = "中文标题",
                content = "第一行\n第二行",
                contentFormat = ContentFormat.PLAIN_TEXT,
                createdAt = 100,
                updatedAt = 200,
                isDeleted = true,
                deletedAt = 300,
            ),
        )
        val output = ByteArrayOutputStream()
        manager.writeBackup(entries, ThemeMode.DARK, output, nowMillis = 400)
        val bytes = output.toByteArray()

        val files = unzip(bytes)
        assertEquals(
            setOf("manifest.json", "entries.json", "settings.json"),
            files.keys,
        )
        assertTrue(files.getValue("manifest.json").contains("\"version\": 1"))

        val restored = manager.readBackup(ByteArrayInputStream(bytes))
        assertEquals(entries, restored.entries)
        assertEquals(ThemeMode.DARK.name, restored.settings.themeMode)
    }

    @Test
    fun unsupportedManifestVersionIsRejected() {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            writeEntry(
                zip,
                "manifest.json",
                """{"version":99,"app":"简日记","createdAt":1}""",
            )
            writeEntry(zip, "entries.json", """{"entries":[]}""")
            writeEntry(zip, "settings.json", """{"themeMode":"SYSTEM"}""")
        }

        assertThrows(BackupException::class.java) {
            manager.readBackup(ByteArrayInputStream(output.toByteArray()))
        }
    }

    private fun unzip(bytes: ByteArray): Map<String, String> {
        val files = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                files[entry.name] = zip.readBytes().toString(StandardCharsets.UTF_8)
            }
        }
        return files
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }
}

