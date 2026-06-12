package com.hgr.authenticator.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Preferences DataStore 封装 —— 键值对偏好存储
 *
 * 用于持久化应用设置项（主题模式、生物识别锁状态）。
 * 数据以键值对形式存储在内部 XML 文件中。
 * 读取通过 Kotlin Flow 暴露，写入通过挂起函数完成。
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_settings")

class PreferencesDataStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val themeMode: Flow<String?> = dataStore.data.map { it[THEME_MODE] }
    val biometricEnabled: Flow<Boolean?> = dataStore.data.map { it[BIOMETRIC_ENABLED] }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }
}
