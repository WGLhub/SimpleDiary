package com.example.simplediary.ui.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.example.simplediary.ui.common.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onBack: () -> Unit,
) {
    val entries by viewModel.entries.collectAsState()
    var purgeEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var clearStep by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("回收站") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = { clearStep = 1 }) {
                            Text("清空")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("回收站是空的", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    end = 16.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = entry.title.ifBlank { "无标题" },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "删除于 ${entry.deletedAt?.let(::formatDateTime) ?: "未知时间"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(onClick = { viewModel.restore(entry.id) }) {
                                    Text("恢复")
                                }
                                TextButton(onClick = { purgeEntry = entry }) {
                                    Text("永久删除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    purgeEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { purgeEntry = null },
            title = { Text("永久删除？") },
            text = { Text("“${entry.title.ifBlank { "无标题" }}”删除后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.purge(entry.id)
                        purgeEntry = null
                    },
                ) {
                    Text("永久删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { purgeEntry = null }) {
                    Text("取消")
                }
            },
        )
    }

    if (clearStep == 1) {
        AlertDialog(
            onDismissRequest = { clearStep = 0 },
            title = { Text("清空回收站？") },
            text = { Text("回收站中的全部日记都将被永久删除。") },
            confirmButton = {
                TextButton(onClick = { clearStep = 2 }) {
                    Text("继续")
                }
            },
            dismissButton = {
                TextButton(onClick = { clearStep = 0 }) {
                    Text("取消")
                }
            },
        )
    }

    if (clearStep == 2) {
        AlertDialog(
            onDismissRequest = { clearStep = 0 },
            title = { Text("再次确认") },
            text = { Text("此操作不可撤销，确定永久清空全部回收站内容吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTrash()
                        clearStep = 0
                    },
                ) {
                    Text("永久清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { clearStep = 0 }) {
                    Text("取消")
                }
            },
        )
    }
}

