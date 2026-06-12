package com.hgr.authenticator.presentation.export

import android.graphics.Bitmap
import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.presentation.base.UiEffect
import com.hgr.authenticator.presentation.base.UiEvent
import com.hgr.authenticator.presentation.base.UiState

object ExportContract {

    data class ExportState(
        val accounts: List<AccountExportItem> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    ) : UiState

    data class AccountExportItem(
        val account: Account,
        val otpAuthUri: String,
        val qrBitmap: Bitmap? = null
    )

    sealed class ExportEvent : UiEvent {
        data object OnLoadAccounts : ExportEvent()
        data class OnQrGenerated(val accountId: Long, val bitmap: Bitmap) : ExportEvent()
        data object OnDismissError : ExportEvent()
    }

    sealed class ExportEffect : UiEffect {
        data class ShowSnackbar(val message: String) : ExportEffect()
    }
}
