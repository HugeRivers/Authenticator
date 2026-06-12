package com.hgr.authenticator.presentation.settings

import androidx.lifecycle.viewModelScope
import com.hgr.authenticator.domain.usecase.GetSettingsUseCase
import com.hgr.authenticator.domain.usecase.ToggleBiometricUseCase
import com.hgr.authenticator.domain.usecase.UpdateThemeUseCase
import com.hgr.authenticator.presentation.base.BaseViewModel
import com.hgr.authenticator.presentation.settings.SettingsContract.SettingsEffect
import com.hgr.authenticator.presentation.settings.SettingsContract.SettingsEvent
import com.hgr.authenticator.presentation.settings.SettingsContract.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页 ViewModel。
 *
 * 管理主题模式与生物识别锁等应用偏好设置。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val toggleBiometricUseCase: ToggleBiometricUseCase
) : BaseViewModel<SettingsState, SettingsEvent, SettingsEffect>(SettingsState()) {

    private val settingsFlow = getSettingsUseCase()

    init {
        observeSettings()
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeSelected -> onThemeSelected(event.themeMode)
            is SettingsEvent.OnBiometricToggle -> onBiometricToggle(event.enabled)
        }
    }

    private fun onThemeSelected(themeMode: com.hgr.authenticator.domain.model.ThemeMode) {
        viewModelScope.launch {
            updateThemeUseCase(themeMode)
        }
    }

    private fun onBiometricToggle(enabled: Boolean) {
        viewModelScope.launch {
            toggleBiometricUseCase(enabled)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsFlow.collect { settings ->
                setState { copy(settings = settings, isLoading = false) }
            }
        }
    }
}
