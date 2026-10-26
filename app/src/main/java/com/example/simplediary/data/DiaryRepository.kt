package com.example.simplediary.data

import androidx.room.withTransaction
import com.example.simplediary.data.local.DiaryDatabase
import com.example.simplediary.data.local.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val database: DiaryDatabase,
) {
    private val dao = database.diaryDao()

    fun observeActiveEntries(): Flow<List<DiaryEntry>> = dao.observeActiveEntries()

    fun searchActiveEntries(query: String): Flow<List<DiaryEntry>> =
        dao.searchActiveEntries(query)

    fun observeTrash(): Flow<List<DiaryEntry>> = dao.observeTrash()

    suspend fun getById(id: Long): DiaryEntry? = dao.getById(id)

    suspend fun insert(entry: DiaryEntry): Long = dao.insert(entry)

    suspend fun update(entry: DiaryEntry) = dao.update(entry)

    suspend fun softDelete(id: Long) = dao.softDelete(id, System.currentTimeMillis())

    suspend fun restore(id: Long) = dao.restore(id, System.currentTimeMillis())

    suspend fun purge(id: Long) = dao.purge(id)

    suspend fun purgeAllTrash() = dao.purgeAllTrash()

    suspend fun getAllForBackup(): List<DiaryEntry> = dao.getAllForBackup()

    suspend fun clearAll() = dao.clearAll()

    suspend fun insertAll(entries: List<DiaryEntry>) = dao.insertAll(entries)

    suspend fun replaceAll(entries: List<DiaryEntry>) {
        database.withTransaction {
            dao.clearAll()
            dao.insertAll(entries)
        }
    }
}

