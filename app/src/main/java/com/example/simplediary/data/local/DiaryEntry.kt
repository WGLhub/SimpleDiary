package com.example.simplediary.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

object ContentFormat {
    // 数据库现在只用纯文本，但保留格式标识，后续可增加 Markdown。
    const val PLAIN_TEXT = "text/plain"
    const val MARKDOWN = "text/markdown"
}

/**
 * 一条日记对应 SQLite 表中的一行。
 *
 * Kotlin 的 data class 类似 Java 的 POJO，但会自动生成 equals、hashCode、
 * toString 和 copy 方法；copy 很适合“复制旧对象并只修改几个字段”的写法。
 *
 * @Entity、@PrimaryKey 是 Room 注解。Room 会在编译时根据这个类生成 SQLite
 * 建表代码，因此字段名就是数据库列名。
 */
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
    // 写入正文时同时记录格式，避免以后改版时无法识别旧数据。
    val contentFormat: String = ContentFormat.PLAIN_TEXT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // 回收站采用软删除：数据仍留在表中，只是标记为已删除。
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)

