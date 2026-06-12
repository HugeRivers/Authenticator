package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.repository.AccountRepository
import javax.inject.Inject

class AddAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Long =
        repository.insertAccount(account)
}
