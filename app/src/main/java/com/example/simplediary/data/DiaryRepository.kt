package com.example.simplediary.data

import androidx.room.withTransaction
import com.example.simplediary.data.local.DiaryDatabase
import com.example.simplediary.data.local.DiaryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository 是 ViewModel 和数据源之间的中间层。
 *
 * 页面不需要知道数据来自 Room 还是别的实现，只调用语义清晰的方法。
 */
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

    /**
     * 恢复备份时，清空和批量插入必须同成功或同失败。
     * withTransaction 对应 SQLite 的事务，可避免中途失败导致只删除一半数据。
     */
    suspend fun replaceAll(entries: List<DiaryEntry>) {
        database.withTransaction {
            dao.clearAll()
            dao.insertAll(entries)
        }
    }
}

