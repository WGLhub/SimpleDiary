package com.example.simplediary.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
@OptIn(ExperimentalCoroutinesApi::class)
class DiaryDaoTest {
    private lateinit var database: DiaryDatabase
    private lateinit var dao: DiaryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            DiaryDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.diaryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun activeSearchTrashRestoreAndPurgeQueriesWork() = runTest {
        val firstId = dao.insert(
            DiaryEntry(
                title = "Kotlin 笔记",
                content = "Compose 学习",
                createdAt = 100,
                updatedAt = 100,
            ),
        )
        val secondId = dao.insert(
            DiaryEntry(
                title = "周末",
                content = "去公园",
                createdAt = 200,
                updatedAt = 200,
            ),
        )

        assertEquals(
            listOf(secondId, firstId),
            dao.observeActiveEntries().first().map { it.id },
        )
        assertEquals(
            listOf(firstId),
            dao.searchActiveEntries("compose").first().map { it.id },
        )

        dao.softDelete(secondId, deletedAt = 300)
        assertEquals(
            listOf(firstId),
            dao.observeActiveEntries().first().map { it.id },
        )
        assertEquals(
            listOf(secondId),
            dao.observeTrash().first().map { it.id },
        )

        dao.restore(secondId, restoredAt = 400)
        assertNull(dao.getById(secondId)?.deletedAt)
        assertEquals(2, dao.observeActiveEntries().first().size)

        dao.softDelete(secondId, deletedAt = 500)
        dao.purge(secondId)
        assertNull(dao.getById(secondId))
    }
}
