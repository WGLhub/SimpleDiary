# 项目架构与 Kotlin 入门

这个项目不复杂，核心可以理解为三层：

```text
Compose 页面（用户看到和操作的界面）
            ↓ 用户事件 / ↑ UI 状态
ViewModel（页面状态和业务协调）
            ↓
Repository（数据访问的统一入口）
            ↓
Room / DataStore（SQLite 数据库 / 简单设置存储）
```

应用启动时，`AppContainer` 创建数据库、Repository、DataStore 和导出器，再交给各个页面使用。这相当于手写一个很小的依赖注入容器，也是项目没有引入 Hilt/Dagger 的原因。

## 目录对应关系

| 目录 | 作用 | 可以类比的东西 |
| --- | --- | --- |
| `ui/home`、`ui/edit` 等 | Compose 页面和对应 ViewModel | Vue 组件 + 页面状态逻辑 |
| `ui/navigation` | 页面路由和 SAF 文件读写入口 | Vue Router + 文件 API |
| `data/local` | Room Entity、DAO、Database | Java 实体类 + DAO/Repository + SQLite |
| `data/DiaryRepository.kt` | 给 ViewModel 提供统一数据接口 | Service/Repository 层 |
| `data/settings` | DataStore 保存主题 | localStorage，但由系统提供并持久化 |
| `export` | TXT、ZIP、备份与恢复 | 文件导出模块 |
| `SimpleDiaryApplication.kt` | 创建并保存全局依赖 | 简单的手写 IoC 容器 |

## 一次“新建日记”如何执行

1. 用户在 `EditScreen` 输入标题或正文。
2. Compose 调用 `EditViewModel.onTitleChange` 或 `onContentChange`。
3. ViewModel 修改 `StateFlow<EditUiState>`，Compose 观察状态并自动刷新界面。
4. 每次输入都会重置一个 1 秒计时器，这就是防抖。
5. 停止输入 1 秒后，ViewModel 调用 Repository，再由 DAO 写入 Room。
6. 第一次插入时 Room 返回自增 `id`，后续修改变成数据库 UPDATE。
7. 返回页面时调用 `flush()`，跳过剩余等待时间，立即保存。
8. 如果新页面标题和正文都为空，则不会创建数据库记录。

保存状态显示为 `UNSAVED / SAVING / SAVED / ERROR`，对应的流在 `ui/edit/EditViewModel.kt`。

## 为什么列表会自动刷新

DAO 的查询返回 `Flow<List<DiaryEntry>>`。你可以把它理解为“可以持续发出新值的管道”：

```text
Room 表变化 → DAO 查询重新执行 → Flow 发出新列表 → ViewModel 暴露 StateFlow
           → Compose collectAsState → 页面自动重组
```

因此删除、恢复或恢复备份后，不需要手动调用“刷新列表”。

## Kotlin 概念对照

| Kotlin 写法 | 含义 | 你已有的知识 |
| --- | --- | --- |
| `val` | 引用不可重新赋值 | 类似 Java `final` |
| `var` | 引用可以重新赋值 | 普通 Java 变量 |
| `String?` | 这个值可能为 `null` | 强制提醒处理空值 |
| `value?.name` | 不为空才访问 | 类似空指针保护 |
| `value ?: "默认值"` | 左边为空时使用右边 | 类似 Python `or` 的常见用法 |
| `fun` | 函数声明 | Java/Python 函数 |
| `data class` | 主要用来保存数据的类 | Java POJO，但自动生成常用方法 |
| `object` | 只有一个实例的对象 | 不需要手写的全局单例 |
| `suspend fun` | 只能在协程中调用的挂起函数 | 类似 `async` 操作，但不会阻塞线程 |
| `Flow` | 可连续发送多个值的异步数据流 | 类似事件流或 Vue 响应式数据 |
| `StateFlow` | 始终保存最新值的数据流 | 页面状态的唯一数据源 |
| `@Composable` | 声明一个 Compose 界面函数 | 类似可重复执行的模板函数 |
| `@Entity`、`@Dao` | 告诉 Room 如何处理类或接口 | ORM 注解 |

### Compose 与 Vue 的相似点

Vue 用模板描述界面，Compose 用 Kotlin 函数描述界面。两者都强调“状态变化后界面更新”：

```text
状态改变 → 界面按新状态重新执行 → 只更新发生变化的部分
```

Compose 的“重新执行”叫重组（Recomposition）。所以页面里不应自己保存唯一数据副本；数据通常放在 ViewModel 中。

## MVVM 中每部分负责什么

- Model：`DiaryEntry`、DAO、Repository、BackupManager 等数据与业务能力。
- View：Compose 的 `HomeScreen`、`EditScreen` 等，只负责显示和收集用户事件。
- ViewModel：保存页面状态，调用 Repository，并把结果转成 UI 可显示的状态。

页面不应该直接执行 SQL；ViewModel 也不应该持有 Activity 或 View，这样代码更容易测试。

## 两个数据存储为什么不同

Room 用来保存结构化的日记列表，支持查询、排序、事务和删除标记。

DataStore 只用来保存少量键值设置，例如主题模式。它比数据库简单，而且同样能通过 Flow 通知界面。

## SAF 文件选择器

Android 不允许普通应用随意访问其他应用的文件。SAF（Storage Access Framework）让用户主动选择一个文件位置，系统返回 `content://` URI：

```text
用户点击导出 → 系统文件选择器 → 返回 URI
→ ContentResolver 打开输出流 → Exporter 写入字节 → 关闭流
```

因此不需要申请存储权限。恢复备份则使用 `OpenDocument` 获取输入流，先校验 JSON 和清单版本，再通过 Room 事务覆盖数据。

## 为什么保留 contentFormat

第一版编辑和导出都只处理纯文本，但数据库已经保存：

- `text/plain`
- 为将来预留的 `text/markdown`

导出层使用 `Exporter` 接口。以后新增 Markdown 时，可以实现一个新 Exporter，而不是重写整个首页、编辑页和备份逻辑。
