package com.example.simplediary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DiaryEntry::class],
    version = 1,
    exportSchema = false,
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}

