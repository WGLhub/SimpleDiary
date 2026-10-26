package com.example.simplediary.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simplediary.data.local.DiaryEntry
import com.example.simplediary.ui.common.formatTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel,
    onBack: () -> Unit,
    onExportTxt: (DiaryEntry) -> Unit,
    onDeleted: () -> Unit,
    onMessage: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var moreExpanded by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }

    fun leaveAfterSave() {
        if (leaving) return
        leaving = true
        scope.launch {
            viewModel.flush()
            onBack()
        }
    }

    BackHandler(onBack = ::leaveAfterSave)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (state.entryId == null) "新建日记" else "编辑日记")
                        Text(
                            text = state.saveStatus.label(state.savedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = ::leaveAfterSave) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { moreExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = moreExpanded,
                            onDismissRequest = { moreExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("导出 TXT") },
                                onClick = {
                                    moreExpanded = false
                                    scope.launch {
                                        val entry = viewModel.prepareForExport()
                                        if (entry == null) {
                                            onMessage("请先输入一些内容")
                                        } else {
                                            onExportTxt(entry)
                                        }
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("移入回收站") },
                                onClick = {
                                    moreExpanded = false
                                    deleteConfirm = true
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "标题",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "${state.content.count { !it.isWhitespace() }} 字",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                OutlinedTextField(
                    value = state.content,
                    onValueChange = viewModel::onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("写点什么……") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
    }

    if (deleteConfirm) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false },
            title = { Text("移入回收站？") },
            text = { Text("删除后可以在设置中的回收站里恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteConfirm = false
                        viewModel.delete(onDeleted)
                    },
                ) {
                    Text("移入回收站")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}

private fun SaveStatus.label(savedAt: Long?): String = when (this) {
    SaveStatus.IDLE -> "等待输入"
    SaveStatus.UNSAVED -> "未保存"
    SaveStatus.SAVING -> "正在保存…"
    SaveStatus.SAVED -> savedAt?.let { "已保存 ${formatTime(it)}" } ?: "已保存"
    SaveStatus.ERROR -> "保存失败"
}
