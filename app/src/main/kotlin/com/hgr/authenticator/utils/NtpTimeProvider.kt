package com.hgr.authenticator.utils

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * NTP time provider that queries pool.ntp.org to detect device clock drift.
 * Uses SNTP (Simple NTP) protocol to get the current time from NTP servers.
 *
 * The offset is cached so repeated calls are fast.
 */
object NtpTimeProvider {

    private var cachedOffsetMs: Long? = null
    private var lastSyncMs: Long = 0
    private const val CACHE_TTL_MS = 60_000L // re-sync every 60s
    private const val NTP_TIMEOUT_MS = 5_000L
    private const val NTP_PORT = 123
    private const val NTP_PACKET_SIZE = 48

    // NTP time origin (Jan 1, 1900) to Unix time (Jan 1, 1970) offset in seconds
    private const val NTP_TO_UNIX_OFFSET = 2_208_988_800L

    /**
     * Returns the estimated correct Unix time in milliseconds.
     * Falls back to System.currentTimeMillis() if NTP query fails.
     */
    suspend fun currentTimeMillis(): Long {
        val offset = getOffsetMs()
        return System.currentTimeMillis() + offset
    }

    /**
     * Returns the time offset in milliseconds (positive = device is behind, needs to add offset).
     * Cached for [CACHE_TTL_MS] to avoid excessive network queries.
     */
    suspend fun getOffsetMs(): Long {
        val now = System.currentTimeMillis()

        // Return cached offset if still fresh
        cachedOffsetMs?.let { offset ->
            if (now - lastSyncMs < CACHE_TTL_MS) {
                return offset
            }
        }

        return try {
            val ntpTime = withContext(Dispatchers.IO) {
                queryNtpTime()
            }
            if (ntpTime > 0) {
                val offset = ntpTime - now
                cachedOffsetMs = offset
                lastSyncMs = now
                offset
            } else {
                cachedOffsetMs ?: 0L
            }
        } catch (_: Exception) {
            cachedOffsetMs ?: 0L
        }
    }

    /**
     * Whether the device time has significant drift (more than ~10 seconds).
     */
    suspend fun hasSignificantDrift(): Boolean {
        val offset = getOffsetMs()
        return kotlin.math.abs(offset) > 10_000L
    }

    /**
     * Force refresh the NTP time check.
     */
    suspend fun refresh() {
        cachedOffsetMs = null
        lastSyncMs = 0
        getOffsetMs()
    }

    /**
     * Query NTP server using SNTP protocol (RFC 4330).
     * Returns the server's Unix time in milliseconds, or 0 on failure.
     */
    private fun queryNtpTime(): Long {
        val socket = DatagramSocket()
        socket.soTimeout = NTP_TIMEOUT_MS.toInt()

        try {
            val address = InetAddress.getByName("pool.ntp.org")
            val packet = ByteArray(NTP_PACKET_SIZE)

            // Initialize NTP request packet (RFC 4330):
            // LI (2 bits) = 0, VN (3 bits) = 3 (NTP v3), Mode (3 bits) = 3 (Client)
            // First byte: 00 011 011 = 0x1B
            packet[0] = 0x1B

            val sendPacket = DatagramPacket(packet, packet.size, address, NTP_PORT)
            socket.send(sendPacket)

            val receivePacket = DatagramPacket(packet, packet.size)
            socket.receive(receivePacket)

            // Extract Transmit Timestamp (bytes 40-43 = seconds, 44-47 = fraction)
            val secondsSince1900 = readInt(packet, 40)

            if (secondsSince1900 == 0L) return 0L

            // Convert from NTP epoch (1900) to Unix epoch (1970)
            val unixTimeSeconds = (secondsSince1900.toLong() and 0xFFFFFFFFL) - NTP_TO_UNIX_OFFSET
            return unixTimeSeconds * 1000L

        } finally {
            socket.close()
        }
    }

    /**
     * Read a 32-bit unsigned integer (big-endian) from a byte array.
     */
    private fun readInt(buffer: ByteArray, offset: Int): Long {
        return ((buffer[offset].toInt() and 0xFF).toLong() shl 24) or
                ((buffer[offset + 1].toInt() and 0xFF).toLong() shl 16) or
                ((buffer[offset + 2].toInt() and 0xFF).toLong() shl 8) or
                (buffer[offset + 3].toInt() and 0xFF).toLong()
    }
}
