package com.hgr.authenticator.utils

import android.net.Uri

/**
 * OTP URI 解析器
 *
 * 将 `otpauth://` URI 解析为 [OtpData] 结构体。
 *
 * ## URI 格式（Google Authenticator Key Uri Format）
 * ```
 * otpauth://TYPE/LABEL?secret=XXXXX&issuer=YYYYY&algorithm=SHA1&digits=6&period=30
 * ```
 *
 * | 参数 | 说明 | 必填 |
 * |------|------|------|
 * | TYPE | `totp`（基于时间）或 `hotp`（基于计数器） | ✓ |
 * | LABEL | `签发方:用户名`，如 `Google:user@gmail.com` | ✓ |
 * | secret | Base32 编码的共享密钥 | ✓ |
 * | issuer | 签发方名称（推荐，覆盖 label 中的签发方） | 推荐 |
 * | algorithm | 哈希算法（SHA1/SHA256/SHA512） | 默认 SHA1 |
 * | digits | 验证码位数 | 默认 6 |
 * | period | TOTP 刷新周期（秒） | 默认 30 |
 *
 * @see <a href="https://github.com/google/google-authenticator/wiki/Key-Uri-Format">Google Authenticator Key URI Format</a>
 */
object OtpUriParser {

    /**
     * OTP URI 解析后的结构化数据
     *
     * @property secret Base32 编码的共享密钥
     * @property issuer 签发方名称（如 "Google"、"GitHub"）
     * @property accountName 账户名称（通常为邮箱）
     * @property algorithm 哈希算法
     * @property digits 验证码位数
     * @property period TOTP 刷新周期（秒）
     */
    data class OtpData(
        val secret: String,
        val issuer: String,
        val accountName: String,
        val algorithm: String = "SHA1",
        val digits: Int = 6,
        val period: Int = 30
    )

    /**
     * 解析 OTP URI 字符串
     *
     * @param uriString 完整的 otpauth URI
     * @return [OtpData] 或 null（解析失败时）
     */
    fun parse(uriString: String): OtpData? {
        return try {
            val uri = Uri.parse(uriString)

            if (uri.scheme != "otpauth") return null
            if (uri.host != "totp" && uri.host != "hotp") return null

            // Path format: /{issuer}:{accountName} or /{accountName}
            val path = uri.path?.trimStart('/') ?: ""
            val (issuer, accountName) = parsePath(path)

            val secret = uri.getQueryParameter("secret") ?: return null
            val queryIssuer = uri.getQueryParameter("issuer")
            val finalIssuer = queryIssuer ?: issuer ?: "Unknown"
            val finalAccountName = if (queryIssuer != null && issuer == null) path else accountName

            val algorithm = uri.getQueryParameter("algorithm") ?: "SHA1"
            val digits = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6
            val period = uri.getQueryParameter("period")?.toIntOrNull() ?: 30

            OtpData(
                secret = secret,
                issuer = finalIssuer,
                accountName = finalAccountName.ifBlank { "$finalIssuer User" },
                algorithm = algorithm,
                digits = digits,
                period = period
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析路径标签中的签发方与用户名
     *
     * 标签格式：`签发方:用户名`
     * 如果标签中没有冒号，则整段视为用户名，签发方为 null。
     */
    private fun parsePath(path: String): Pair<String?, String> {
        val colonIndex = path.indexOf(':')
        return if (colonIndex > 0) {
            val issuer = path.substring(0, colonIndex)
            val accountName = path.substring(colonIndex + 1)
            issuer to accountName
        } else {
            null to path
        }
    }
}
