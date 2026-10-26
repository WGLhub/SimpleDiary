package com.example.simplediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.simplediary.data.settings.ThemeMode
import com.example.simplediary.ui.navigation.SimpleDiaryNavHost
import com.example.simplediary.ui.theme.SimpleDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SimpleDiaryApplication).container

        setContent {
            // collectAsState 把 DataStore 的 Flow 转成 Compose 可观察状态。
            // 主题变化后 Compose 自动重组，不需要手动重启 Activity。
            val themeMode by container.settingsRepository.themeMode.collectAsState(
                initial = ThemeMode.SYSTEM,
            )
            SimpleDiaryTheme(themeMode = themeMode) {
                SimpleDiaryNavHost(container = container)
            }
        }
    }
}

