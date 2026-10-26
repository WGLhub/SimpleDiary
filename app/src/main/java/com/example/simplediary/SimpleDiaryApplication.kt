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

/**
 * Application 文件中的 container 相当于一个很小的手动依赖注入容器。
 *
 * 数据库、Repository 和导出器在应用进程中只创建一次。相比 Hilt/Dagger，
 * 这种写法更直观，适合当前规模。
 */
class SimpleDiaryApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    // Room.databaseBuilder 创建 SQLite 数据库连接；DAO 由 Room 自动实现。
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

