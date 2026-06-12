package com.hgr.authenticator.data.repository

import com.hgr.authenticator.data.local.PreferencesDataStore
import com.hgr.authenticator.domain.model.Settings
import com.hgr.authenticator.domain.model.ThemeMode
import com.hgr.authenticator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val preferences: PreferencesDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<Settings> = combine(
        preferences.themeMode,
        preferences.biometricEnabled
    ) { theme, biometric ->
        Settings(
            themeMode = when (theme) {
                "light" -> ThemeMode.Light
                "dark" -> ThemeMode.Dark
                else -> ThemeMode.System
            },
            biometricEnabled = biometric ?: false
        )
    }

    override suspend fun setThemeMode(theme: String) {
        preferences.setThemeMode(theme)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        preferences.setBiometricEnabled(enabled)
    }
}
