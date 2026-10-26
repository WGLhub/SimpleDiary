package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.ui.common.formatDateTime
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipExporter(
    private val exporter: Exporter,
) {
    fun export(entries: List<DiaryEntry>, output: OutputStream) {
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            val indexLines = StringBuilder()
                .appendLine("简日记导出")
                .appendLine("条目数：${entries.size}")
                .appendLine()

            entries.forEachIndexed { index, entry ->
                val safeTitle = FileNames.sanitizeTitle(entry.title)
                val path = "entries/${"%04d".format(index + 1)}_${entry.id}_$safeTitle.${exporter.extension}"
                zip.putNextEntry(ZipEntry(path))
                zip.write(exporter.render(entry).toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
                indexLines.appendLine(
                    "${index + 1}. $safeTitle（${formatDateTime(entry.updatedAt)}）",
                )
            }

            zip.putNextEntry(ZipEntry("index.txt"))
            zip.write(indexLines.toString().toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }
    }
}

