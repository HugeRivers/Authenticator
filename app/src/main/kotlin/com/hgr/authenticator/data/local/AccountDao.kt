package com.hgr.authenticator.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO —— 账户数据访问对象
 *
 * 提供 [AccountEntity] 的数据库读写操作。
 * 查询结果以 Kotlin Flow 形式返回，支持响应式 UI 更新。
 * 搜索功能支持按签发方（issuer）和用户名（name）模糊匹配。
 */
@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE issuer LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)

    @Update
    suspend fun update(account: AccountEntity)
}
