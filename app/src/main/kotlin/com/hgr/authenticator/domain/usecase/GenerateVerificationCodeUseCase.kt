package com.hgr.authenticator.domain.usecase

import com.hgr.authenticator.utils.VerificationCodeGenerator
import javax.inject.Inject

/**
 * 生成 TOTP 验证码用例
 *
 * 调用 [VerificationCodeGenerator] 基于共享密钥生成当前时间窗口的 6 位动态验证码。
 * 同时计算周期剩余时间（用于 UI 进度条倒计时）。
 *
 * 输出格式：验证码每 3 位插入空格（如 "123 456"）方便用户阅读。
 */
class GenerateVerificationCodeUseCase @Inject constructor() {

    /**
     * 验证码生成结果
     *
     * @property code 格式化的验证码（如 "123 456"）
     * @property timeRemaining 当前周期剩余秒数
     * @property progress 进度比值（0.0 ~ 1.0），用于倒计时进度条
     */
    data class VerificationCodeResult(
        val code: String,
        val timeRemaining: Int,
        val progress: Float
    )

    operator fun invoke(secret: String, period: Int = 30): VerificationCodeResult {
        val generator = VerificationCodeGenerator(secret, period)
        val code = generator.generate()
        val timeRemaining = generator.getTimeRemaining()
        val progress = timeRemaining.toFloat() / period
        return VerificationCodeResult(
            code = code.chunked(3).joinToString(" "),
            timeRemaining = timeRemaining,
            progress = progress
        )
    }
}
