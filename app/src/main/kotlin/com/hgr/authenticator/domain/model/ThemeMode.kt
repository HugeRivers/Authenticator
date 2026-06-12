package com.hgr.authenticator.domain.model

/**
 * 主题模式
 *
 * 控制应用界面的明暗主题：
 * - [System]: 跟随系统设置自动切换
 * - [Light]: 强制浅色模式
 * - [Dark]: 强制深色模式
 */
sealed class ThemeMode {
    data object System : ThemeMode()
    data object Light : ThemeMode()
    data object Dark : ThemeMode()
}
