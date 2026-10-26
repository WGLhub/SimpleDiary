warning: in the working copy of 'README.md', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/MainActivity.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/SimpleDiaryApplication.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/data/DiaryRepository.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/data/local/DiaryDao.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/data/local/DiaryEntry.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/data/settings/SettingsRepository.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/export/BackupManager.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/export/Exporter.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/ui/edit/EditViewModel.kt', LF will be replaced by CRLF the next time Git touches it
warning: in the working copy of 'app/src/main/java/com/example/simplediary/ui/navigation/SimpleDiaryNavHost.kt', LF will be replaced by CRLF the next time Git touches it
[1mdiff --git a/README.md b/README.md[m
[1mindex 048e9c4..91f7a2e 100644[m
[1m--- a/README.md[m
[1m+++ b/README.md[m
[36m@@ -2,7 +2,14 @@[m
 [m
 一个仅在本机保存纯文本日记的 Android App，使用 Kotlin、Jetpack Compose、Room、MVVM、Navigation Compose、DataStore 和 kotlinx.serialization。[m
 [m
[31m-## 构建[m
[32m+[m[32m## 先看这里[m
[32m+[m
[32m+[m[32m如果你有 Java、HTML/CSS/JavaScript、Vue、Python 或 C 的基础，但刚接触 Kotlin 和 Android，请先阅读：[m
[32m+[m
[32m+[m[32m- [项目架构与 Kotlin 入门说明](docs/ARCHITECTURE.md)[m
[32m+[m[32m- [如何打包 APK](docs/BUILD_APK.md)[m
[32m+[m
[32m+[m[32m## 快速构建[m
 [m
 需要 JDK 17 与 Android SDK Platform 33。[m
 [m
[1mdiff --git a/app/src/main/java/com/example/simplediary/MainActivity.kt b/app/src/main/java/com/example/simplediary/MainActivity.kt[m
[1mindex 6e9be63..2fb97d4 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/MainActivity.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/MainActivity.kt[m
[36m@@ -15,6 +15,8 @@[m [mclass MainActivity : ComponentActivity() {[m
         val container = (application as SimpleDiaryApplication).container[m
 [m
         setContent {[m
[32m+[m[32m            // collectAsState 把 DataStore 的 Flow 转成 Compose 可观察状态。[m
[32m+[m[32m            // 主题变化后 Compose 自动重组，不需要手动重启 Activity。[m
             val themeMode by container.settingsRepository.themeMode.collectAsState([m
                 initial = ThemeMode.SYSTEM,[m
             )[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/SimpleDiaryApplication.kt b/app/src/main/java/com/example/simplediary/SimpleDiaryApplication.kt[m
[1mindex 5856c87..dbda5c4 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/SimpleDiaryApplication.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/SimpleDiaryApplication.kt[m
[36m@@ -10,6 +10,12 @@[m [mimport com.example.simplediary.export.BackupManager[m
 import com.example.simplediary.export.TxtExporter[m
 import com.example.simplediary.export.ZipExporter[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * Application 文件中的 container 相当于一个很小的手动依赖注入容器。[m
[32m+[m[32m *[m
[32m+[m[32m * 数据库、Repository 和导出器在应用进程中只创建一次。相比 Hilt/Dagger，[m
[32m+[m[32m * 这种写法更直观，适合当前规模。[m
[32m+[m[32m */[m
 class SimpleDiaryApplication : Application() {[m
     val container: AppContainer by lazy { AppContainer(this) }[m
 }[m
[36m@@ -17,6 +23,7 @@[m [mclass SimpleDiaryApplication : Application() {[m
 class AppContainer(context: Context) {[m
     private val applicationContext = context.applicationContext[m
 [m
[32m+[m[32m    // Room.databaseBuilder 创建 SQLite 数据库连接；DAO 由 Room 自动实现。[m
     private val database = Room.databaseBuilder([m
         applicationContext,[m
         DiaryDatabase::class.java,[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/data/DiaryRepository.kt b/app/src/main/java/com/example/simplediary/data/DiaryRepository.kt[m
[1mindex a0a18a5..ee57f47 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/data/DiaryRepository.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/data/DiaryRepository.kt[m
[36m@@ -5,6 +5,11 @@[m [mimport com.example.simplediary.data.local.DiaryDatabase[m
 import com.example.simplediary.data.local.DiaryEntry[m
 import kotlinx.coroutines.flow.Flow[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * Repository 是 ViewModel 和数据源之间的中间层。[m
[32m+[m[32m *[m
[32m+[m[32m * 页面不需要知道数据来自 Room 还是别的实现，只调用语义清晰的方法。[m
[32m+[m[32m */[m
 class DiaryRepository([m
     private val database: DiaryDatabase,[m
 ) {[m
[36m@@ -37,6 +42,10 @@[m [mclass DiaryRepository([m
 [m
     suspend fun insertAll(entries: List<DiaryEntry>) = dao.insertAll(entries)[m
 [m
[32m+[m[32m    /**[m
[32m+[m[32m     * 恢复备份时，清空和批量插入必须同成功或同失败。[m
[32m+[m[32m     * withTransaction 对应 SQLite 的事务，可避免中途失败导致只删除一半数据。[m
[32m+[m[32m     */[m
     suspend fun replaceAll(entries: List<DiaryEntry>) {[m
         database.withTransaction {[m
             dao.clearAll()[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/data/local/DiaryDao.kt b/app/src/main/java/com/example/simplediary/data/local/DiaryDao.kt[m
[1mindex 6895431..f541ce8 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/data/local/DiaryDao.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/data/local/DiaryDao.kt[m
[36m@@ -7,6 +7,12 @@[m [mimport androidx.room.Query[m
 import androidx.room.Update[m
 import kotlinx.coroutines.flow.Flow[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * DAO（Data Access Object）只负责数据库操作。[m
[32m+[m[32m *[m
[32m+[m[32m * 这里声明的是接口，Room 在构建时自动生成实现类。方法上的 SQL 会在生成代码中[m
[32m+[m[32m * 执行，不需要手写 JDBC 或 Cursor。[m
[32m+[m[32m */[m
 @Dao[m
 interface DiaryDao {[m
     @Query([m
[36m@@ -16,6 +22,7 @@[m [minterface DiaryDao {[m
         ORDER BY updatedAt DESC, id DESC[m
         """,[m
     )[m
[32m+[m[32m    // 返回 Flow 后，表数据发生变化时 Room 会自动发出新列表，页面无需手动刷新。[m
     fun observeActiveEntries(): Flow<List<DiaryEntry>>[m
 [m
     @Query([m
[36m@@ -40,12 +47,15 @@[m [minterface DiaryDao {[m
     @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")[m
     suspend fun getById(id: Long): DiaryEntry?[m
 [m
[32m+[m[32m    // suspend 表示调用方必须从协程中执行。Room 会把数据库 I/O 放到合适线程，[m
[32m+[m[32m    // 避免阻塞 Android 的 UI 线程。[m
     @Insert(onConflict = OnConflictStrategy.ABORT)[m
     suspend fun insert(entry: DiaryEntry): Long[m
 [m
     @Update[m
     suspend fun update(entry: DiaryEntry)[m
 [m
[32m+[m[32m    // 移入回收站不是 DELETE，而是把 isDeleted 改成 1 并记录删除时间。[m
     @Query([m
         """[m
         UPDATE diary_entries[m
[36m@@ -64,6 +74,7 @@[m [minterface DiaryDao {[m
     )[m
     suspend fun restore(id: Long, restoredAt: Long)[m
 [m
[32m+[m[32m    // purge 才真正执行 DELETE，只能在回收站中对已删除记录使用。[m
     @Query("DELETE FROM diary_entries WHERE id = :id AND isDeleted = 1")[m
     suspend fun purge(id: Long)[m
 [m
[1mdiff --git a/app/src/main/java/com/example/simplediary/data/local/DiaryEntry.kt b/app/src/main/java/com/example/simplediary/data/local/DiaryEntry.kt[m
[1mindex 9378aef..e327d40 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/data/local/DiaryEntry.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/data/local/DiaryEntry.kt[m
[36m@@ -6,10 +6,20 @@[m [mimport androidx.room.PrimaryKey[m
 import kotlinx.serialization.Serializable[m
 [m
 object ContentFormat {[m
[32m+[m[32m    // 数据库现在只用纯文本，但保留格式标识，后续可增加 Markdown。[m
     const val PLAIN_TEXT = "text/plain"[m
     const val MARKDOWN = "text/markdown"[m
 }[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * 一条日记对应 SQLite 表中的一行。[m
[32m+[m[32m *[m
[32m+[m[32m * Kotlin 的 data class 类似 Java 的 POJO，但会自动生成 equals、hashCode、[m
[32m+[m[32m * toString 和 copy 方法；copy 很适合“复制旧对象并只修改几个字段”的写法。[m
[32m+[m[32m *[m
[32m+[m[32m * @Entity、@PrimaryKey 是 Room 注解。Room 会在编译时根据这个类生成 SQLite[m
[32m+[m[32m * 建表代码，因此字段名就是数据库列名。[m
[32m+[m[32m */[m
 @Serializable[m
 @Entity([m
     tableName = "diary_entries",[m
[36m@@ -23,9 +33,11 @@[m [mdata class DiaryEntry([m
     val id: Long = 0,[m
     val title: String = "",[m
     val content: String = "",[m
[32m+[m[32m    // 写入正文时同时记录格式，避免以后改版时无法识别旧数据。[m
     val contentFormat: String = ContentFormat.PLAIN_TEXT,[m
     val createdAt: Long = System.currentTimeMillis(),[m
     val updatedAt: Long = System.currentTimeMillis(),[m
[32m+[m[32m    // 回收站采用软删除：数据仍留在表中，只是标记为已删除。[m
     val isDeleted: Boolean = false,[m
     val deletedAt: Long? = null,[m
 )[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/data/settings/SettingsRepository.kt b/app/src/main/java/com/example/simplediary/data/settings/SettingsRepository.kt[m
[1mindex 17578b0..ac11460 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/data/settings/SettingsRepository.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/data/settings/SettingsRepository.kt[m
[36m@@ -10,6 +10,7 @@[m [mimport kotlinx.coroutines.flow.catch[m
 import kotlinx.coroutines.flow.first[m
 import kotlinx.coroutines.flow.map[m
 [m
[32m+[m[32m// DataStore 实例必须全局唯一，因此放在文件级扩展属性中。[m
 private val Context.settingsDataStore by preferencesDataStore(name = "settings")[m
 [m
 enum class ThemeMode {[m
[36m@@ -28,6 +29,7 @@[m [mclass SettingsRepository([m
 ) {[m
     private val themeModeKey = stringPreferencesKey("theme_mode")[m
 [m
[32m+[m[32m    // data 是一个 Flow：每次写入设置后都会重新发出值，MainActivity 会立即重组主题。[m
     val themeMode: Flow<ThemeMode> = context.settingsDataStore.data[m
         .catch { error ->[m
             if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/export/BackupManager.kt b/app/src/main/java/com/example/simplediary/export/BackupManager.kt[m
[1mindex 28a5022..0e091e3 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/export/BackupManager.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/export/BackupManager.kt[m
[36m@@ -17,6 +17,7 @@[m [mimport kotlinx.serialization.json.Json[m
 [m
 const val BACKUP_VERSION = 1[m
 [m
[32m+[m[32m// @Serializable 让 kotlinx.serialization 在编译期生成 JSON 编解码代码。[m
 @Serializable[m
 data class BackupManifest([m
     val version: Int,[m
[36m@@ -43,6 +44,7 @@[m [mclass BackupException(message: String, cause: Throwable? = null) :[m
     IllegalArgumentException(message, cause)[m
 [m
 class BackupManager {[m
[32m+[m[32m    // ignoreUnknownKeys 允许新版本备份多出字段时，旧版本仍能读取已知字段。[m
     private val json = Json {[m
         prettyPrint = true[m
         encodeDefaults = true[m
[36m@@ -89,6 +91,7 @@[m [mclass BackupManager {[m
             ?: throw BackupException("备份缺少 settings.json")[m
 [m
         val manifest = decodeOrThrow<BackupManifest>(manifestText)[m
[32m+[m[32m        // 版本校验失败时拒绝恢复，防止用不兼容的数据覆盖当前数据库。[m
         if (manifest.version != BACKUP_VERSION) {[m
             throw BackupException([m
                 "不支持的备份版本：${manifest.version}，当前支持版本：$BACKUP_VERSION",[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/export/Exporter.kt b/app/src/main/java/com/example/simplediary/export/Exporter.kt[m
[1mindex 40a3fae..3b19140 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/export/Exporter.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/export/Exporter.kt[m
[36m@@ -3,6 +3,12 @@[m [mpackage com.example.simplediary.export[m
 import com.example.simplediary.data.local.DiaryEntry[m
 import java.io.OutputStream[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * 导出格式的抽象接口。[m
[32m+[m[32m *[m
[32m+[m[32m * 当前由 TxtExporter 实现；以后增加 MarkdownExporter 时，ZipExporter 和页面[m
[32m+[m[32m * 都可以继续依赖这个接口，而不需要到处判断具体文件格式。[m
[32m+[m[32m */[m
 interface Exporter {[m
     val extension: String[m
     val mimeType: String[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/ui/edit/EditViewModel.kt b/app/src/main/java/com/example/simplediary/ui/edit/EditViewModel.kt[m
[1mindex 336a5c1..59c7462 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/ui/edit/EditViewModel.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/ui/edit/EditViewModel.kt[m
[36m@@ -32,10 +32,17 @@[m [mdata class EditUiState([m
     val savedAt: Long? = null,[m
 )[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * 编辑页的 ViewModel，负责输入状态、自动保存和新建日记拿到数据库 id。[m
[32m+[m[32m *[m
[32m+[m[32m * ViewModel 的生命周期比 Composable 长：旋转屏幕造成界面重建时，正在编辑的[m
[32m+[m[32m * 数据仍保存在 ViewModel 中。协程由 viewModelScope 管理，页面销毁时会自动取消。[m
[32m+[m[32m */[m
 class EditViewModel([m
     private val repository: DiaryRepository,[m
     initialEntryId: Long?,[m
 ) : ViewModel() {[m
[32m+[m[32m    // MutableStateFlow 只能在 ViewModel 内修改；state 对外只读。[m
     private val _state = MutableStateFlow([m
         EditUiState([m
             entryId = initialEntryId,[m
[36m@@ -108,6 +115,7 @@[m [mclass EditViewModel([m
     }[m
 [m
     suspend fun flush() {[m
[32m+[m[32m        // 返回页面或导出前取消防抖计时器，立即把最新内容写入数据库。[m
         debounceJob?.cancel()[m
         debounceJob = null[m
         saveNow()[m
[36m@@ -131,6 +139,7 @@[m [mclass EditViewModel([m
     }[m
 [m
     private fun scheduleSave() {[m
[32m+[m[32m        // 防抖：连续输入时不断取消旧计时器，停止输入 1 秒后才真正保存。[m
         debounceJob?.cancel()[m
         debounceJob = viewModelScope.launch {[m
             delay(1_000)[m
[36m@@ -139,10 +148,12 @@[m [mclass EditViewModel([m
     }[m
 [m
     private suspend fun saveNow() {[m
[32m+[m[32m        // Mutex 保证防抖保存、返回时保存等调用不会同时修改同一条记录。[m
         saveMutex.withLock {[m
             if (!loaded || deleted) return[m
 [m
             val snapshot = _state.value[m
[32m+[m[32m            // 新建页面如果标题和正文都为空，不创建数据库记录。[m
             if (snapshot.title.isBlank() && snapshot.content.isBlank() && currentId == null) {[m
                 _state.update { it.copy(saveStatus = SaveStatus.IDLE) }[m
                 return[m
[36m@@ -161,6 +172,7 @@[m [mclass EditViewModel([m
                         createdAt = now,[m
                         updatedAt = now,[m
                     )[m
[32m+[m[32m                    // Room 插入成功后会返回自增 id；后续保存转为 UPDATE。[m
                     val newId = repository.insert(newEntry)[m
                     currentId = newId[m
                     createdAt = now[m
[1mdiff --git a/app/src/main/java/com/example/simplediary/ui/navigation/SimpleDiaryNavHost.kt b/app/src/main/java/com/example/simplediary/ui/navigation/SimpleDiaryNavHost.kt[m
[1mindex 9cbe414..a2cc56a 100644[m
[1m--- a/app/src/main/java/com/example/simplediary/ui/navigation/SimpleDiaryNavHost.kt[m
[1m+++ b/app/src/main/java/com/example/simplediary/ui/navigation/SimpleDiaryNavHost.kt[m
[36m@@ -51,6 +51,8 @@[m [mfun SimpleDiaryNavHost(container: AppContainer) {[m
         }[m
     }[m
 [m
[32m+[m[32m    // CreateDocument 会打开 Android 系统文件选择器，返回一个 content:// URI。[m
[32m+[m[32m    // 应用只通过 ContentResolver 写入这个 URI，不需要存储权限。[m
     val createTextDocument = rememberLauncherForActivityResult([m
         contract = ActivityResultContracts.CreateDocument("text/plain"),[m
     ) { uri ->[m
[36m@@ -59,6 +61,7 @@[m [mfun SimpleDiaryNavHost(container: AppContainer) {[m
         if (uri == null || entry == null) return@rememberLauncherForActi