package com.example.simplediary

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.simplediary.data.DiaryRepository
import com.example.simplediary.data.local.DiaryDatabase
import com.example.simplediary.data.settings.SettingsRepository
import com.example.simplediary.export.BackupManager
import com.example.simplediary.export.TxtExporter
import com.example.simplediary.export.ZipExporter

class SimpleDiaryApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    private val database = Room.databaseBuilder(
        applicationContext,
        DiaryDatabase::class.java,
        "simple_diary.db",
    ).build()

    val diaryRepository = DiaryRepository(database)
    val settingsRepository = SettingsRepository(applicationContext)
    val txtExporter = TxtExporter()
    val zipExporter = ZipExporter(txtExporter)
    val backupManager = BackupManager()
}

