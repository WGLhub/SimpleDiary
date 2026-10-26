package com.example.simplediary.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

object ContentFormat {
    const val PLAIN_TEXT = "text/plain"
    const val MARKDOWN = "text/markdown"
}

@Serializable
@Entity(
    tableName = "diary_entries",
    indices = [
        Index(value = ["isDeleted", "updatedAt"]),
        Index(value = ["isDeleted", "deletedAt"]),
    ],
)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val contentFormat: String = ContentFormat.PLAIN_TEXT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)

