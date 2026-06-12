package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.model.Settings
import com.hgr.authenticator.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> = repository.getSettings()
}
