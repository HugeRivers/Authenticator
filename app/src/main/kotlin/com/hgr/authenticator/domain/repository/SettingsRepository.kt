package com.hgr.authenticator.domain.repository

import com.hgr.authenticator.domain.model.Settings
import kotlinx.coroutines.flow.Flow

/**
 * 设置数据仓库接口
 *
 * 定义 [Settings] 的读写操作契约。
 * 实现通过 [com.hgr.authenticator.data.local.PreferencesDataStore]
 * 以 Preferences DataStore 方式持久化存储。
 */
interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun setThemeMode(theme: String)
    suspend fun setBiometricEnabled(enabled: Boolean)
}
