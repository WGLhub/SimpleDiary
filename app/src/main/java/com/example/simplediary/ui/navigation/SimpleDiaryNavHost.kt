package com.example.simplediary.ui.navigation

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.simplediary.AppContainer
import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.data.settings.ThemeMode
import com.example.simplediary.export.FileNames
import com.example.simplediary.ui.common.SimpleViewModelFactory
import com.example.simplediary.ui.edit.EditScreen
import com.example.simplediary.ui.edit.EditViewModel
import com.example.simplediary.ui.home.HomeScreen
import com.example.simplediary.ui.home.HomeViewModel
import com.example.simplediary.ui.search.SearchScreen
import com.example.simplediary.ui.search.SearchViewModel
import com.example.simplediary.ui.settings.SettingsScreen
import com.example.simplediary.ui.settings.SettingsViewModel
import com.example.simplediary.ui.trash.TrashScreen
import com.example.simplediary.ui.trash.TrashViewModel
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SimpleDiaryNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingExportEntry by remember { mutableStateOf<DiaryEntry?>(null) }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // CreateDocument 会打开 Android 系统文件选择器，返回一个 content:// URI。
    // 应用只通过 ContentResolver 写入这个 URI，不需要存储权限。
    val createTextDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        val entry = pendingExportEntry
        pendingExportEntry = null
        if (uri == null || entry == null) return@rememberLauncherForActivityResult

        scope.launch {
            // 文件读写属于阻塞 I/O，必须切换到 IO 线程，不能占用 Compose 的 UI 线程。
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    context.openRequiredOutputStream(uri)
                        .use { output -> container.txtExporter.write(entry, output) }
                }.exceptionOrNull()
            }
            showMessage(if (error == null) "TXT 已导出" else "导出失败：${error.readableMessage()}")
        }
    }

    val createAllZipDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val entries = container.diaryRepository
                        .getAllForBackup()
                        .filterNot { it.isDeleted }
                    context.openRequiredOutputStream(uri).use { output ->
                        container.zipExporter.export(entries, output)
                    }
                }.exceptionOrNull()
            }
            showMessage(if (error == null) "全部日记已导出" else "导出失败：${error.readableMessage()}")
        }
    }

    val createBackupDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val entries = container.diaryRepository.getAllForBackup()
                    val themeMode = container.settingsRepository.currentThemeMode()
                    context.openRequiredOutputStream(uri).use { output ->
                        container.backupManager.writeBackup(entries, themeMode, output)
                    }
                }.exceptionOrNull()
            }
            showMessage(if (error == null) "备份已保存" else "备份失败：${error.readableMessage()}")
        }
    }

    val openBackupDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val backup = context.contentResolver.openInputStream(uri)?.use {
                        container.backupManager.readBackup(it)
                    } ?: throw IOException("无法读取备份文件")
                    // 日记清空和批量插入在同一次事务中完成。
                    container.diaryRepository.replaceAll(backup.entries)
                    container.settingsRepository.setThemeMode(
                        ThemeMode.fromStorage(backup.settings.themeMode),
                    )
                }
            }
            showMessage(
                if (result.isSuccess) "备份恢复完成" else "恢复失败：${result.exceptionOrNull().readableMessage()}",
            )
        }
    }

    val exportEntry: (DiaryEntry) -> Unit = { entry ->
        pendingExportEntry = entry
        createTextDocument.launch(FileNames.forEntry(entry))
    }
    val exportAll: () -> Unit = {
        createAllZipDocument.launch(FileNames.allEntriesZip())
    }
    val backupAll: () -> Unit = {
        createBackupDocument.launch(FileNames.backupZip())
    }
    val restoreBackup: () -> Unit = {
        openBackupDocument.launch(arrayOf("application/zip", "application/octet-stream"))
    }

    // NavHost 根据路由决定显示哪个 Screen；每个路由创建自己的 ViewModel。
    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                key = "home",
                factory = SimpleViewModelFactory {
                    HomeViewModel(container.diaryRepository)
                },
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNewEntry = { navController.navigate("edit/-1") },
                onOpenEntry = { id -> navController.navigate("edit/$id") },
                onOpenSearch = { navController.navigate("search") },
                onOpenSettings = { navController.navigate("settings") },
                onExportTxt = exportEntry,
                onMessage = ::showMessage,
                snackbarHostState = snackbarHostState,
            )
        }

        composable(
            route = "edit/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getLong("id") ?: -1L
            val entryId = rawId.takeIf { it > 0 }
            val editViewModel: EditViewModel = viewModel(
                key = "edit-${entryId ?: "new"}",
                factory = SimpleViewModelFactory {
                    EditViewModel(container.diaryRepository, entryId)
                },
            )
            EditScreen(
                viewModel = editViewModel,
                onBack = { navController.popBackStack() },
                onExportTxt = exportEntry,
                onDeleted = { navController.popBackStack() },
                onMessage = ::showMessage,
                snackbarHostState = snackbarHostState,
            )
        }

        composable("search") {
            val searchViewModel: SearchViewModel = viewModel(
                key = "search",
                factory = SimpleViewModelFactory {
                    SearchViewModel(container.diaryRepository)
                },
            )
            SearchScreen(
                viewModel = searchViewModel,
                onBack = { navController.popBackStack() },
                onOpenEntry = { id -> navController.navigate("edit/$id") },
            )
        }

        composable("trash") {
            val trashViewModel: TrashViewModel = viewModel(
                key = "trash",
                factory = SimpleViewModelFactory {
                    TrashViewModel(container.diaryRepository)
                },
            )
            TrashScreen(
                viewModel = trashViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel(
                key = "settings",
                factory = SimpleViewModelFactory {
                    SettingsViewModel(container.settingsRepository)
                },
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onOpenTrash = { navController.navigate("trash") },
                onExportAll = exportAll,
                onBackup = backupAll,
                onRestore = restoreBackup,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

private fun Context.openRequiredOutputStream(uri: android.net.Uri) =
    contentResolver.openOutputStream(uri)
        ?: throw IOException("系统未提供可写入的文件")

private fun Throwable?.readableMessage(): String =
    this?.message?.takeIf { it.isNotBlank() } ?: "未知错误"
