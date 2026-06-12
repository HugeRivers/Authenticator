package com.hgr.authenticator.data.repository

import com.hgr.authenticator.data.local.AccountDao
import com.hgr.authenticator.data.local.AccountEntity
import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> =
        accountDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun searchAccounts(query: String): Flow<List<Account>> =
        accountDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getAccountById(id: Long): Account? =
        accountDao.getById(id)?.toDomain()

    override suspend fun insertAccount(account: Account): Long =
        accountDao.insert(account.toEntity())

    override suspend fun deleteAccount(id: Long) =
        accountDao.delete(id)

    override suspend fun updateAccount(account: Account) =
        accountDao.update(account.toEntity())

    private fun AccountEntity.toDomain() = Account(
        id = id,
        issuer = issuer,
        name = name,
        secret = secret,
        color = color,
        icon = icon,
        algorithm = algorithm,
        digits = digits,
        period = period,
        createdAt = createdAt
    )

    private fun Account.toEntity() = AccountEntity(
        id = id,
        issuer = issuer,
        name = name,
        secret = secret,
        color = color,
        icon = icon,
        algorithm = algorithm,
        digits = digits,
        period = period,
        createdAt = createdAt
    )
}
