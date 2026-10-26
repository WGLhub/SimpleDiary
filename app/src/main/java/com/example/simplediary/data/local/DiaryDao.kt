package com.example.simplediary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query(
        """
        SELECT * FROM diary_entries
        WHERE isDeleted = 0
        ORDER BY updatedAt DESC, id DESC
        """,
    )
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: DiaryEntry): Long

    @Update
    suspend fun update(entry: DiaryEntry)

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

