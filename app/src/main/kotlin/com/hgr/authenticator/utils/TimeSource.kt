package com.hgr.authenticator.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 全局时间源（融合 NTP 校正时间与系统时间）
 *
 * TOTP 算法要求客户端与服务端时间偏差在 ±30 秒以内。
 * 如果设备未启用 NTP 同步（许多 Android 设备默认未开启），
 * 设备时钟漂移可能导致生成的验证码与服务端不匹配。
 *
 * 此组件在后台通过 NTP 协议获取标准时间，计算与系统时钟的偏移量。
 * 同步前使用系统时间作为回退，同步完成后自动使用校正时间。
 *
 * 使用方式：在 Application.onCreate() 中调用 [initialize] 启动首次同步。
 *
 * @see NtpTimeProvider NTP 协议实现
 */
object TimeSource {

    private var ntpOffsetMs: Long = 0L
    private var lastSyncMs: Long = 0L
    private var syncAttempted: Boolean = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 获取当前时间（毫秒）
     *
     * 若 NTP 已同步成功，返回 NTP 校正后的时间；
     * 若尚未同步，返回系统时间。
     * 此方法不会阻塞（同步在后台异步进行）。
     */
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis() + ntpOffsetMs
    }

    /**
     * 获取最近一次 NTP 同步的偏移量（毫秒）
     * 0 表示尚未完成同步。
     */
    fun getLastKnownOffsetMs(): Long = ntpOffsetMs

    /**
     * 初始化 —— 启动首次 NTP 同步
     *
     * 应在 Application.onCreate() 中调用一次。
     * 同步在后台 IO 协程中执行，不影响主线程。
     */
    fun initialize() {
        if (syncAttempted) return
        syncAttempted = true
        scope.launch {
            try {
                val offset = NtpTimeProvider.getOffsetMs()
                ntpOffsetMs = offset
                lastSyncMs = System.currentTimeMillis()
            } catch (_: Exception) {
                // Use system time as fallback
            }
        }
    }

    /**
     * 强制刷新 NTP 时间
     *
     * 在以下场景可调用：
     * - 用户手动触发同步
     * - 检测到设备时间发生变化
     * - 距离上次同步超过一定时间间隔
     */
    fun refresh() {
        scope.launch {
            try {
                val offset = NtpTimeProvider.getOffsetMs()
                ntpOffsetMs = offset
                lastSyncMs = System.currentTimeMillis()
            } catch (_: Exception) { }
        }
    }

    /**
     * 判断设备时钟是否存在显著漂移（>10 秒）
     *
     * 当 NTP 偏移量绝对值超过 10 秒时返回 true。
     * 可用于提示用户校准设备时间。
     */
    fun hasSignificantDrift(): Boolean {
        if (ntpOffsetMs == 0L) return false // no data yet
        return kotlin.math.abs(ntpOffsetMs) > 10_000L
    }
}
