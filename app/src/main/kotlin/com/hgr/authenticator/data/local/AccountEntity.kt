package com.hgr.authenticator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 数据库实体 —— 账户表
 *
 * 与 [com.hgr.authenticator.domain.model.Account] 领域模型对应。
 * 通过 [AccountRepositoryImpl] 中的 toEntity() / toDomain() 映射转换。
 *
 * @property id 自增主键
 * @property issuer 签发方名称
 * @property name 账户名称
 * @property secret Base32 编码的共享密钥（核心敏感数据）
 * @property color 列表展示主题色
 * @property icon 图标字符
 * @property algorithm 哈希算法（SHA1/SHA256/SHA512）
 * @property digits 验证码位数
 * @property period TOTP 刷新周期（秒）
 * @property createdAt 创建时间戳
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val issuer: String,
    val name: String,
    val secret: String,
    val color: String,
    val icon: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)
