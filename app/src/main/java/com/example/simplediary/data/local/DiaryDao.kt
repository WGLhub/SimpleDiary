package com.example.simplediary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO（Data Access Object）只负责数据库操作。
 *
 * 这里声明的是接口，Room 在构建时自动生成实现类。方法上的 SQL 会在生成代码中
 * 执行，不需要手写 JDBC 或 Cursor。
 */
@Dao
interface DiaryDao {
    @Query(
        """
        SELECT * FROM diary_entries
        WHERE isDeleted = 0
        ORDER BY updatedAt DESC, id DESC
        """,
    )
    // 返回 Flow 后，表数据发生变化时 Room 会自动发出新列表，页面无需手动刷新。
    fun observeActiveEntries(): Flow<List<DiaryEntry>>

    @Query(
        """
        SELECT * FROM diary_entries
        WHERE isDeleted = 0
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC, id DESC
        """,
    )
    fun searchActiveEntries(query: String): Flow<List<DiaryEntry>>

    @Query(
        """
        SELECT * FROM diary_entries
        WHERE isDeleted = 1
        ORDER BY deletedAt DESC, id DESC
        """,
    )
    fun observeTrash(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiaryEntry?

    // suspend 表示调用方必须从协程中执行。Room 会把数据库 I/O 放到合适线程，
    // 避免阻塞 Android 的 UI 线程。
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: DiaryEntry): Long

    @Update
    suspend fun update(entry: DiaryEntry)

    // 移入回收站不是 DELETE，而是把 isDeleted 改成 1 并记录删除时间。
    @Query(
        """
        UPDATE diary_entries
        SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt
        WHERE id = :id AND isDeleted = 0
        """,
    )
    suspend fun softDelete(id: Long, deletedAt: Long)

    @Query(
        """
        UPDATE diary_entries
        SET isDeleted = 0, deletedAt = NULL, updatedAt = :restoredAt
        WHERE id = :id AND isDeleted = 1
        """,
    )
    suspend fun restore(id: Long, restoredAt: Long)

    // purge 才真正执行 DELETE，只能在回收站中对已删除记录使用。
    @Query("DELETE FROM diary_entries WHERE id = :id AND isDeleted = 1")
    suspend fun purge(id: Long)

    @Query("DELETE FROM diary_entries WHERE isDeleted = 1")
    suspend fun purgeAllTrash()

    @Query("SELECT * FROM diary_entries ORDER BY id ASC")
    suspend fun getAllForBackup(): List<DiaryEntry>

    @Query("DELETE FROM diary_entries")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DiaryEntry>)
}

