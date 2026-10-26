package com.example.simplediary.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore 实例必须全局唯一，因此放在文件级扩展属性中。
private val Context.settingsDataStore by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            values().firstOrNull { it.name == value } ?: SYSTEM
    }
}

class SettingsRepository(
    private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    // data 是一个 Flow：每次写入设置后都会重新发出值，MainActivity 会立即重组主题。
    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences -> ThemeMode.fromStorage(preferences[themeModeKey]) }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[themeModeKey] = mode.name
        }
    }

    suspend fun currentThemeMode(): ThemeMode = themeMode.first()
}

