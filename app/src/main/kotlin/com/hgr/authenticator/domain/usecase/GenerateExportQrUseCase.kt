package com.hgr.authenticator.domain.usecase

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.hgr.authenticator.domain.model.Account
import javax.inject.Inject

class GenerateExportQrUseCase @Inject constructor() {

    operator fun invoke(account: Account, sizePx: Int = DEFAULT_SIZE_PX): Result<Bitmap> {
        return try {
            val uri = buildOtpAuthUri(account)
            val bitmap = encodeQrCode(uri, sizePx)
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildOtpAuthUri(account: Account): String {
        val label = if (account.name.isBlank()) {
            account.issuer
        } else {
            "${account.issuer}:${account.name}"
        }
        return buildString {
            append("otpauth://totp/")
            append(UriEncoder.encode(label))
            append("?secret=")
            append(UriEncoder.encode(account.secret))
            append("&issuer=")
            append(UriEncoder.encode(account.issuer))
            if (account.algorithm.isNotBlank()) {
                append("&algorithm=")
                append(account.algorithm)
            }
            append("&digits=")
            append(account.digits)
            append("&period=")
            append(account.period)
        }
    }

    private fun encodeQrCode(content: String, sizePx: Int): Bitmap {
        val writer = QRCodeWriter()
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        return bitmap
    }

    private object UriEncoder {
        fun encode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")
    }

    companion object {
        private const val DEFAULT_SIZE_PX = 512
    }
}
