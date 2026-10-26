package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.data.settings.ThemeMode
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val BACKUP_VERSION = 1

// @Serializable 让 kotlinx.serialization 在编译期生成 JSON 编解码代码。
@Serializable
data class BackupManifest(
    val version: Int,
    val app: String = "简日记",
    val createdAt: Long,
)

@Serializable
data class BackupEntries(
    val entries: List<DiaryEntry>,
)

@Serializable
data class BackupSettings(
    val themeMode: String = ThemeMode.SYSTEM.name,
)

data class RestoredBackup(
    val entries: List<DiaryEntry>,
    val settings: BackupSettings,
)

class BackupException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

class BackupManager {
    // ignoreUnknownKeys 允许新版本备份多出字段时，旧版本仍能读取已知字段。
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun writeBackup(
        entries: List<DiaryEntry>,
        themeMode: ThemeMode,
        output: OutputStream,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            writeTextEntry(
                zip,
                "manifest.json",
                json.encodeToString(
                    BackupManifest(
                        version = BACKUP_VERSION,
                        createdAt = nowMillis,
                    ),
                ),
            )
            writeTextEntry(
                zip,
                "entries.json",
                json.encodeToString(BackupEntries(entries)),
            )
            writeTextEntry(
                zip,
                "settings.json",
                json.encodeToString(BackupSettings(themeMode.name)),
            )
        }
    }

    fun readBackup(input: InputStream): RestoredBackup {
        val files = readZipFiles(input)
        val manifestText = files["manifest.json"]
            ?: throw BackupException("备份缺少 manifest.json")
        val entriesText = files["entries.json"]
            ?: throw BackupException("备份缺少 entries.json")
        val settingsText = files["settings.json"]
            ?: throw BackupException("备份缺少 settings.json")

        val manifest = decodeOrThrow<BackupManifest>(manifestText)
        // 版本校验失败时拒绝恢复，防止用不兼容的数据覆盖当前数据库。
        if (manifest.version != BACKUP_VERSION) {
            throw BackupException(
                "不支持的备份版本：${manifest.version}，当前支持版本：$BACKUP_VERSION",
            )
        }

        val backupEntries = decodeOrThrow<BackupEntries>(entriesText)
        val backupSettings = decodeOrThrow<BackupSettings>(settingsText)
        return RestoredBackup(
            entries = backupEntries.entries,
            settings = backupSettings,
        )
    }

    private fun writeTextEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun readZipFiles(input: InputStream): Map<String, String> {
        val acceptedNames = setOf("manifest.json", "entries.json", "settings.json")
        val files = mutableMapOf<String, String>()

        try {
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.name in acceptedNames) {
                        val bytes = zip.readBytes()
                        if (bytes.size > MAX_BACKUP_ENTRY_BYTES) {
                            throw BackupException("备份文件内容过大")
                        }
                        files[entry.name] = bytes.toString(StandardCharsets.UTF_8)
                    }
                    zip.closeEntry()
                }
            }
        } catch (error: BackupException) {
            throw error
        } catch (error: Exception) {
            throw BackupException("无法读取备份文件", error)
        }
        return files
    }

    private inline fun <reified T> decodeOrThrow(text: String): T =
        try {
            json.decodeFromString(text)
        } catch (error: Exception) {
            throw BackupException("备份 JSON 无效", error)
        }

    private companion object {
        const val MAX_BACKUP_ENTRY_BYTES = 64 * 1024 * 1024
    }
}
