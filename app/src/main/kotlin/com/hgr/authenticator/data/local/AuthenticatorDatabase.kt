package com.hgr.authenticator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room 数据库 —— 认证器本地存储
 *
 * 仅包含 [AccountEntity] 一张表。
 * 通过 [accountDao] 提供数据访问。
 * 使用 Room 的 Flow 查询特性实现响应式数据更新。
 */
@Database(
    entities = [AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AuthenticatorDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}
