package com.hgr.authenticator.domain.model

/**
 * 双因素认证（2FA）账户模型
 *
 * 每个账户对应一个基于 TOTP（RFC 6238）协议的认证条目。
 * 用户通过扫描服务商提供的 QR 码或手动输入密钥来创建账户，
 * 随后应用会基于当前时间 + 共享密钥定期生成 6 位动态验证码。
 *
 * @property id 数据库主键，自动生成
 * @property issuer 签发方名称，如 "Google"、"GitHub"
 * @property name 账户名称/用户名，通常为邮箱地址
 * @property secret Base32 编码的共享密钥（TOTP 算法的核心凭证）
 * @property color 列表展示时的主题色
 * @property icon 图标字符（取 issuer 首字母）
 * @property algorithm 哈希算法，默认为 SHA1
 * @property digits 验证码位数，默认为 6
 * @property period TOTP 刷新周期（秒），默认为 30
 * @property createdAt 创建时间戳
 */
data class Account(
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
