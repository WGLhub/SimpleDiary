package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.ui.common.formatDateTime
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class TxtExporter : Exporter {
    override val extension: String = "txt"
    override val mimeType: String = "text/plain"

    override fun render(entry: DiaryEntry): String {
        val displayTitle = entry.title.ifBlank { "无标题" }
        return buildString {
            appendLine(displayTitle)
            appendLine()
            appendLine("创建时间：${formatDateTime(entry.createdAt)}")
            appendLine("更新时间：${formatDateTime(entry.updatedAt)}")
            appendLine()
            appendLine("----------------------------------------")
            appendLine()
            append(entry.content)
            if (!entry.content.endsWith("\n")) appendLine()
        }
    }

    override fun write(entry: DiaryEntry, output: OutputStream) {
        OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
            writer.write(render(entry))
            writer.flush()
        }
    }
}

