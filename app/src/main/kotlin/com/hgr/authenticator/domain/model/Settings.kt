package com.hgr.authenticator.domain.model

/**
 * 应用设置模型
 *
 * 持久化存储的用户偏好设置，通过 Preferences DataStore 读写。
 *
 * @property themeMode 主题模式（跟随系统 / 浅色 / 深色）
 * @property biometricEnabled 是否启用生物识别锁
 * @property version 应用版本号
 */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.System,
    val biometricEnabled: Boolean = false,
    val version: String = "1.0.0"
)
