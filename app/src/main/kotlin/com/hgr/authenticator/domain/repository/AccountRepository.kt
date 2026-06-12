package com.hgr.authenticator.domain.repository

import com.hgr.authenticator.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * 账户数据仓库接口
 *
 * 定义 [Account] 的 CRUD 操作契约。
 * 数据层（[com.hgr.authenticator.data.repository.AccountRepositoryImpl]）负责实现此接口。
 * 业务层（UseCase）通过此接口访问数据，不依赖具体实现。
 */
interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun searchAccounts(query: String): Flow<List<Account>>
    suspend fun getAccountById(id: Long): Account?
    suspend fun insertAccount(account: Account): Long
    suspend fun deleteAccount(id: Long)
    suspend fun updateAccount(account: Account)
}
