package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.repository.SettingsRepository
import javax.inject.Inject

class ToggleBiometricUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setBiometricEnabled(enabled)
    }
}
