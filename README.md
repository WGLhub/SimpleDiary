# 简日记

一个仅在本机保存纯文本日记的 Android App，使用 Kotlin、Jetpack Compose、Room、MVVM、Navigation Compose、DataStore 和 kotlinx.serialization。

## 先看这里

如果你有 Java、HTML/CSS/JavaScript、Vue、Python 或 C 的基础，但刚接触 Kotlin 和 Android，请先阅读：

- [项目架构与 Kotlin 入门说明](docs/ARCHITECTURE.md)
- [如何打包 APK](docs/BUILD_APK.md)

## 快速构建

需要 JDK 17 与 Android SDK Platform 33。

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Debug APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。

## 数据与权限

- 日记保存在应用私有 Room 数据库中。
- 主题保存在 DataStore 中。
- 导出、备份和恢复只使用系统 SAF 文件选择器，不申请存储权限。
- Manifest 不声明 `INTERNET` 权限，不连接网络或云服务。
- 自动系统备份与设备迁移备份已关闭，完整数据备份应使用应用内“备份全部数据”。

## 备份格式

备份 ZIP 包含 `manifest.json`、`entries.json` 和 `settings.json`。恢复前会校验 `manifest.json` 中的版本，并在单个 Room 事务内覆盖全部日记数据。

## 第一版边界

- 编辑器只编辑纯文本；数据库已保留 `contentFormat` 字段，导出层通过 `Exporter` 接口隔离，便于后续增加 Markdown。
- ZIP 内的日记正文目前统一导出为 UTF-8 TXT。
