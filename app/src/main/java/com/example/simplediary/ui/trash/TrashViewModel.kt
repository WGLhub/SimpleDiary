package com.example.simplediary.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplediary.data.DiaryRepository
import com.example.simplediary.data.local.DiaryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(
    private val repository: DiaryRepository,
) : ViewModel() {
    val entries: StateFlow<List<DiaryEntry>> = repository.observeTrash()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun restore(entryId: Long) {
        viewModelScope.launch {
            repository.restore(entryId)
        }
    }

    fun purge(entryId: Long) {
        viewModelScope.launch {
            repository.purge(entryId)
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            repository.purgeAllTrash()
        }
    }
}

