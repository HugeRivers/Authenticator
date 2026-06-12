package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.model.ThemeMode
import com.hgr.authenticator.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateThemeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        val modeString = when (themeMode) {
            ThemeMode.System -> "system"
            ThemeMode.Light -> "light"
            ThemeMode.Dark -> "dark"
        }
        repository.setThemeMode(modeString)
    }
}
