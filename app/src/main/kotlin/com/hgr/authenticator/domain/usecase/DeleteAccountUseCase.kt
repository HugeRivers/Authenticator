package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(id: Long) =
        repository.deleteAccount(id)
}
