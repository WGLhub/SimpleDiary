package com.example.simplediary.export

import com.example.simplediary.data.local.DiaryEntry
import java.io.OutputStream

interface Exporter {
    val extension: String
    val mimeType: String

    fun render(entry: DiaryEntry): String

    fun write(entry: DiaryEntry, output: OutputStream)
}

