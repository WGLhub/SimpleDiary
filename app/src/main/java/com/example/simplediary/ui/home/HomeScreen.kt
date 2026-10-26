package com.example.simplediary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.ui.common.DiaryEntryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNewEntry: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onExportTxt: (DiaryEntry) -> Unit,
    onMessage: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val entries by viewModel.entries.collectAsState()
    var menuEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<DiaryEntry?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButtonPosition = FabPosition.Center,
        topBar = {
            TopAppBar(
                title = { Text("简日记") },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        floatingActionButton = {
            // 底部额外留出空间，让新增按钮比默认 FAB 位置略微上移。
            FloatingActionButton(
                onClick = onNewEntry,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建日记")
            }
        },
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "还没有日记",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "写下今天的第一句话吧",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    Box {
                        DiaryEntryCard(
                            entry = entry,
                            onClick = { onOpenEntry(entry.id) },
                            onLongClick = { menuEntry = entry },
                        )
                        DropdownMenu(
                            expanded = menuEntry?.id == entry.id,
                            onDismissRequest = { menuEntry = null },
                        ) {
                            DropdownMenuItem(
                                text = { Text("导出 TXT") },
                                onClick = {
                                    menuEntry = null
                                    onExportTxt(entry)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("移入回收站") },
                                onClick = {
                                    menuEntry = null
                                    deletingEntry = entry
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    deletingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            title = { Text("移入回收站？") },
            text = { Text("“${entry.title.ifBlank { "无标题" }}”将在回收站中保留，之后可以恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(entry.id)
                        deletingEntry = null
                        onMessage("已移入回收站")
                    },
                ) {
                    Text("移入回收站")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) {
                    Text("取消")
                }
            },
        )
    }
}

