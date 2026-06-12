package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> = repository.getAllAccounts()
}
