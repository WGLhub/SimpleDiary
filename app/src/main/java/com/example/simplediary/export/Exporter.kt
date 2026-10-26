package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import java.io.OutputStream

/**
 * 导出格式的抽象接口。
 *
 * 当前由 TxtExporter 实现；以后增加 MarkdownExporter 时，ZipExporter 和页面
 * 都可以继续依赖这个接口，而不需要到处判断具体文件格式。
 */
interface Exporter {
    val extension: String
    val mimeType: String

    fun render(entry: DiaryEntry): String

    fun write(entry: DiaryEntry, output: OutputStream)
}

