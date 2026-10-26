package com.example.simplediary.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplediary.data.DiaryRepository
import com.example.simplediary.data.local.DiaryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DiaryRepository,
) : ViewModel() {
    val entries: StateFlow<List<DiaryEntry>> = repository.observeActiveEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun delete(entryId: Long) {
        viewModelScope.launch {
            repository.softDelete(entryId)
        }
    }
}

