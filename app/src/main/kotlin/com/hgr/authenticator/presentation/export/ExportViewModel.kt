package com.hgr.authenticator.presentation.export

import androidx.lifecycle.viewModelScope
import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.usecase.GenerateExportQrUseCase
import com.hgr.authenticator.domain.usecase.GetAccountsUseCase
import com.hgr.authenticator.presentation.base.BaseViewModel
import com.hgr.authenticator.presentation.export.ExportContract.AccountExportItem
import com.hgr.authenticator.presentation.export.ExportContract.ExportEffect
import com.hgr.authenticator.presentation.export.ExportContract.ExportEvent
import com.hgr.authenticator.presentation.export.ExportContract.ExportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val generateExportQrUseCase: GenerateExportQrUseCase
) : BaseViewModel<ExportState, ExportEvent, ExportEffect>(ExportState()) {

    init {
        onEvent(ExportEvent.OnLoadAccounts)
    }

    override fun onEvent(event: ExportEvent) {
        when (event) {
            is ExportEvent.OnLoadAccounts -> loadAccounts()
            is ExportEvent.OnQrGenerated -> onQrGenerated(event.accountId, event.bitmap)
            is ExportEvent.OnDismissError -> setState { copy(errorMessage = null) }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                getAccountsUseCase().collect { accounts ->
                    val items = accounts.map { account ->
                        AccountExportItem(
                            account = account,
                            otpAuthUri = buildOtpAuthUri(account)
                        )
                    }
                    setState { copy(accounts = items, isLoading = false) }
                    generateQrBitmaps(items)
                }
            } catch (e: Exception) {
                setState { copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun generateQrBitmaps(items: List<AccountExportItem>) {
        items.forEach { item ->
            viewModelScope.launch {
                generateExportQrUseCase(item.account)
                    .onSuccess { bitmap ->
                        setState {
                            copy(
                                accounts = accounts.map { current ->
                                    if (current.account.id == item.account.id) {
                                        current.copy(qrBitmap = bitmap)
                                    } else {
                                        current
                                    }
                                }
                            )
                        }
                    }
                    .onFailure {
                        setEffect(ExportEffect.ShowSnackbar("Failed to generate QR for ${item.account.issuer}"))
                    }
            }
        }
    }

    private fun onQrGenerated(accountId: Long, bitmap: android.graphics.Bitmap) {
        setState {
            copy(
                accounts = accounts.map { item ->
                    if (item.account.id == accountId) item.copy(qrBitmap = bitmap) else item
                }
            )
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
            append(java.net.URLEncoder.encode(label, "UTF-8"))
            append("?secret=")
            append(java.net.URLEncoder.encode(account.secret, "UTF-8"))
            append("&issuer=")
            append(java.net.URLEncoder.encode(account.issuer, "UTF-8"))
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
}
