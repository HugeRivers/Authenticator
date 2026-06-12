package com.hgr.authenticator.presentation.settings

import com.hgr.authenticator.domain.model.Settings
import com.hgr.authenticator.domain.model.ThemeMode
import com.hgr.authenticator.presentation.base.UiEffect
import com.hgr.authenticator.presentation.base.UiEvent
import com.hgr.authenticator.presentation.base.UiState

/**
 * 设置页 MVI 契约。
 */
object SettingsContract {

    /**
     * 设置页 UI 状态。
     */
    data class SettingsState(
        val settings: Settings = Settings(),
        val isLoading: Boolean = true
    ) : UiState

    /**
     * 设置页 UI 事件。
     */
    sealed class SettingsEvent : UiEvent {
        data class OnThemeSelected(val themeMode: ThemeMode) : SettingsEvent()
        data class OnBiometricToggle(val enabled: Boolean) : SettingsEvent()
    }

    /**
     * 设置页 UI 副作用。
     */
    sealed class SettingsEffect : UiEffect {
        data class ShowSnackbar(val message: String) : SettingsEffect()
    }
}
