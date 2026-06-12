package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(query: String): Flow<List<Account>> =
        if (query.isBlank()) repository.getAllAccounts()
        else repository.searchAccounts(query)
}
