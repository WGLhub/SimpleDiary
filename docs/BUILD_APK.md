# 打包 APK

推荐第一次使用 Android Studio 打包；熟悉后再用命令行。

## 方式一：Android Studio（推荐）

1. 安装 Android Studio。安装向导会安装所需的 JDK。
2. 在 SDK Manager 中安装 Android SDK Platform 33。
3. 选择 **Open**，打开项目目录 `D:\Android\simpleDiary`。
4. 等待右下角的 Gradle Sync 完成。
5. 连接手机或启动模拟器后，点击运行按钮即可安装调试版。
6. 选择 **Build → Build Bundle(s) / APK(s) → Build APK(s)**。
7. 构建完成后点击通知中的 **locate**，或打开：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 方式二：Windows 命令行

需要完整 JDK 17（必须包含 `jlink.exe`）和 Android SDK Platform 33。

```powershell
$env:JAVA_HOME = "你的 JDK17 安装目录"
.\gradlew.bat assembleDebug
```

执行测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

在 Git Bash、Linux 或 macOS 中：

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

APK 都在 `app/build/outputs/apk/debug/app-debug.apk`。

## 安装到手机

最简单的方法是把这个 APK 文件传到手机，点击后允许“安装未知应用”。

如果电脑已安装 Android SDK 的 `adb` 并且手机开启了 USB 调试：

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Debug 与 Release 的区别

- Debug APK：开发测试使用，使用自动生成的调试签名，安装方便，不适合直接发布。
- Release APK：正式发布使用，需要自己的签名密钥。建议使用 Android Studio 的 **Generate Signed Bundle / APK** 向导，并妥善保管 keystore 和密码。

发布到应用商店通常还需要正式签名和版本号；如果只是安装到自己的手机测试，使用 Debug APK 即可。

## 常见构建问题

### 找不到 Java

Android Studio 自带的 JDK 通常位于 Android Studio 安装目录的 `jbr` 文件夹。它必须是完整 JDK，而不只是精简 JRE。

### 找不到 Android SDK

项目根目录的 `local.properties` 应指向 SDK，例如：

```properties
sdk.dir=D\:\\Android\\工具\\Sdk
```

这个文件与本机路径有关，通常不提交到 Git。

### 依赖下载失败

第一次构建需要联网下载 Gradle 和 AndroidX 依赖。完成一次后，本机会缓存依赖。

### 改了代码但没有生效

先执行 Clean Project，再重新 Build；或在命令行运行 `.\gradlew.bat clean assembleDebug`。
