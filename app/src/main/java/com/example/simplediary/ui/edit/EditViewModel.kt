package com.example.simplediary.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplediary.data.DiaryRepository
import com.example.simplediary.data.local.ContentFormat
import com.example.simplediary.data.local.DiaryEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class SaveStatus {
    IDLE,
    UNSAVED,
    SAVING,
    SAVED,
    ERROR,
}

data class EditUiState(
    val entryId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = true,
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val savedAt: Long? = null,
)

/**
 * 编辑页的 ViewModel，负责输入状态、自动保存和新建日记拿到数据库 id。
 *
 * ViewModel 的生命周期比 Composable 长：旋转屏幕造成界面重建时，正在编辑的
 * 数据仍保存在 ViewModel 中。协程由 viewModelScope 管理，页面销毁时会自动取消。
 */
class EditViewModel(
    private val repository: DiaryRepository,
    initialEntryId: Long?,
) : ViewModel() {
    // MutableStateFlow 只能在 ViewModel 内修改；state 对外只读。
    private val _state = MutableStateFlow(
        EditUiState(
            entryId = initialEntryId,
            isLoading = initialEntryId != null,
        ),
    )
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    private val saveMutex = Mutex()
    private var debounceJob: Job? = null
    private var currentId: Long? = initialEntryId
    private var createdAt: Long? = null
    private var contentFormat: String = ContentFormat.PLAIN_TEXT
    private var revision: Long = 0
    private var loaded = initialEntryId == null
    private var deleted = false

    init {
        if (initialEntryId != null) {
            viewModelScope.launch {
                val entry = repository.getById(initialEntryId)
                if (entry == null || entry.isDeleted) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            saveStatus = SaveStatus.ERROR,
                        )
                    }
                    loaded = false
                } else {
                    currentId = entry.id
                    createdAt = entry.createdAt
                    contentFormat = entry.contentFormat
                    loaded = true
                    _state.value = EditUiState(
                        entryId = entry.id,
                        title = entry.title,
                        content = entry.content,
                        isLoading = false,
                        saveStatus = SaveStatus.IDLE,
                        savedAt = entry.updatedAt,
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        if (!loaded || deleted) return
        _state.update {
            it.copy(
                title = value,
                saveStatus = SaveStatus.UNSAVED,
            )
        }
        revision += 1
        scheduleSave()
    }

    fun onContentChange(value: String) {
        if (!loaded || deleted) return
        _state.update {
            it.copy(
                content = value,
                saveStatus = SaveStatus.UNSAVED,
            )
        }
        revision += 1
        scheduleSave()
    }

    suspend fun flush() {
        // 返回页面或导出前取消防抖计时器，立即把最新内容写入数据库。
        debounceJob?.cancel()
        debounceJob = null
        saveNow()
    }

    suspend fun prepareForExport(): DiaryEntry? {
        flush()
        return currentId?.let { repository.getById(it) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            debounceJob?.cancel()
            debounceJob = null
            deleted = true
            saveMutex.withLock {
                currentId?.let { repository.softDelete(it) }
            }
            onDeleted()
        }
    }

    private fun scheduleSave() {
        // 防抖：连续输入时不断取消旧计时器，停止输入 1 秒后才真正保存。
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(1_000)
            saveNow()
        }
    }

    private suspend fun saveNow() {
        // Mutex 保证防抖保存、返回时保存等调用不会同时修改同一条记录。
        saveMutex.withLock {
            if (!loaded || deleted) return

            val snapshot = _state.value
            // 新建页面如果标题和正文都为空，不创建数据库记录。
            if (snapshot.title.isBlank() && snapshot.content.isBlank() && currentId == null) {
                _state.update { it.copy(saveStatus = SaveStatus.IDLE) }
                return
            }

            val revisionToSave = revision
            _state.update { it.copy(saveStatus = SaveStatus.SAVING) }
            try {
                val now = System.currentTimeMillis()
                val id = currentId
                if (id == null) {
                    val newEntry = DiaryEntry(
                        title = snapshot.title,
                        content = snapshot.content,
                        contentFormat = contentFormat,
                        createdAt = now,
                        updatedAt = now,
                    )
                    // Room 插入成功后会返回自增 id；后续保存转为 UPDATE。
                    val newId = repository.insert(newEntry)
                    currentId = newId
                    createdAt = now
                    _state.update { it.copy(entryId = newId) }
                } else {
                    val entry = DiaryEntry(
                        id = id,
                        title = snapshot.title,
                        content = snapshot.content,
                        contentFormat = contentFormat,
                        createdAt = createdAt ?: now,
                        updatedAt = now,
                        isDeleted = false,
                        deletedAt = null,
                    )
                    repository.update(entry)
                }

                val latest = revision == revisionToSave
                _state.update {
                    it.copy(
                        saveStatus = if (latest) SaveStatus.SAVED else SaveStatus.UNSAVED,
                        savedAt = now,
                    )
                }
                if (!latest) scheduleSave()
            } catch (_: Exception) {
                _state.update { it.copy(saveStatus = SaveStatus.ERROR) }
            }
        }
    }
}
