package com.hgr.authenticator.utils

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

/**
 * TOTP（Time-based One-Time Password）验证码生成器
 *
 * 实现 RFC 6238（TOTP）和 RFC 4226（HOTP）标准。
 *
 * ## 算法流程
 * 1. **时间分片**：将当前 Unix 时间戳除以 [period]（通常 30 秒），得到计数器值
 * 2. **HMAC-SHA1**：使用 Base32 解码后的密钥对计数器进行 HMAC-SHA1 运算
 * 3. **动态截断（Dynamic Truncation）**：取 HMAC 结果的最后一个字节的低 4 位作为偏移量，
 *    从该偏移处取 4 字节，清除最高位（符号位），得到 31 位整数
 * 4. **取模**：对该整数取 10^6 的模，得到 6 位验证码
 *
 * ## 安全说明
 * - 密钥（secret）是 Base32 编码的共享密钥，应安全存储
 * - 每 [period] 秒验证码刷新一次，降低重放攻击风险
 * - 设备时间偏差超过 30 秒会导致验证码不匹配（参见 [TimeSource]）
 *
 * @property secret Base32 编码的共享密钥
 * @property period TOTP 刷新周期（秒），通常为 30
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238">RFC 6238 - TOTP</a>
 * @see <a href="https://tools.ietf.org/html/rfc4226">RFC 4226 - HOTP</a>
 */
class VerificationCodeGenerator(private val secret: String, private val period: Int = 30) {

    private val digits = 6
    private val algorithm = "HmacSHA1"

    /**
     * 生成当前时间窗口的 TOTP 验证码
     *
     * @return 6 位验证码字符串，不足 6 位前补零
     */
    fun generate(): String {
        val timeIndex = TimeSource.currentTimeMillis() / 1000 / period
        return generateHOTP(timeIndex)
    }

    /**
     * 获取当前周期剩余时间（秒）
     *
     * @return 距离下次验证码刷新还有多少秒
     */
    fun getTimeRemaining(): Int {
        val currentTime = TimeSource.currentTimeMillis() / 1000
        return period - (currentTime % period).toInt()
    }

    /**
     * 基于计数器生成 HOTP 验证码
     *
     * 实现 RFC 4226 §5.3 定义的截断算法：
     * 1. 使用 HMAC-SHA1 对计数器（8 字节大端）进行签名
     * 2. 取 HMAC 结果最后一个字节的低 4 位作为 DT（偏移量）
     * 3. 从 DT 位置取 4 字节，清除最高位，得到 31 位整数
     * 4. 模 10^digits 得到验证码
     *
     * @param counter 计数器值（TOTP 中为时间分片，HOTP 中为递增序列号）
     * @return digits 位验证码
     */
    private fun generateHOTP(counter: Long): String {
        val decodedSecret = base32Decode(secret)
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(decodedSecret, algorithm))
        val hash = mac.doFinal(counterBytes)
        // RFC 4226 §5.4: DT (Dynamic Truncation)
        val offset = hash.last().toInt() and 0x0f
        // 注意：Kotlin 中缀运算符 shl/or 优先级相同，必须加括号保证顺序
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)
        // Ensure binary is treated as unsigned 31-bit (RFC 4226 §5.4)
        val otp = (binary and 0x7FFFFFFF) % 10.0.pow(digits).toInt()
        return otp.toString().padStart(digits, '0')
    }

    /**
     * Base32 解码
     *
     * 将 RFC 4648 Base32 编码的字符串解码为字节数组。
     * 每 5 位字符映射为 8 位字节，支持标准的 A-Z2-7 字符集。
     * 解码时自动过滤掉无效字符（如空格、连字符）。
     *
     * @param input Base32 编码的字符串（不区分大小写）
     * @return 解码后的密钥字节数组
     */
    private fun base32Decode(input: String): ByteArray {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = input.uppercase().filter { it in base32Chars }
        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (char in cleaned) {
            buffer = (buffer shl 5) or base32Chars.indexOf(char)
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                output.add((buffer shr bitsLeft and 0xff).toByte())
            }
        }

        return output.toByteArray()
    }
}
