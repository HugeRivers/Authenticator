package com.hgr.authenticator.presentation.addaccount

import androidx.lifecycle.viewModelScope
import com.hgr.authenticator.R
import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.usecase.AddAccountUseCase
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.AddAccountEffect
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.AddAccountEvent
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.AddAccountState
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.ScanState
import com.hgr.authenticator.presentation.base.BaseViewModel
import com.hgr.authenticator.utils.OtpUriParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * 添加账户页 ViewModel。
 *
 * 支持扫码添加和手动输入两种方式，管理扫码状态机与表单提交。
 */
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val addAccountUseCase: AddAccountUseCase
) : BaseViewModel<AddAccountState, AddAccountEvent, AddAccountEffect>(AddAccountState()) {

    private var scanValidationJob: Job? = null

    override fun onEvent(event: AddAccountEvent) {
        when (event) {
            is AddAccountEvent.OnTabSelected -> onTabSelected(event.tab)
            is AddAccountEvent.OnAccountNameChange -> setState { copy(accountName = event.name, errorMessage = null) }
            is AddAccountEvent.OnSecretKeyChange -> setState { copy(secretKey = event.key, errorMessage = null) }
            is AddAccountEvent.OnUserNameChange -> setState { copy(userName = event.name) }
            is AddAccountEvent.OnAddAccount -> onAddAccount()
            is AddAccountEvent.OnCameraPermissionResult -> onCameraPermissionResult(event.granted)
            is AddAccountEvent.OnCameraPermissionRequested -> {}
            is AddAccountEvent.OnQrCodeDetected -> onQrCodeDetected(event.qrData)
            is AddAccountEvent.OnDismissError -> setState { copy(errorMessage = null, scanState = ScanState.Scanning) }
        }
    }

    private fun onTabSelected(tab: AddAccountContract.Tab) {
        setState { copy(selectedTab = tab, errorMessage = null) }
    }

    private fun onCameraPermissionResult(granted: Boolean) {
        setState {
            copy(
                hasCameraPermission = granted,
                scanState = if (granted) ScanState.Scanning else ScanState.Idle
            )
        }
    }

    private fun onQrCodeDetected(qrData: String) {
        if (currentState.scanState !is ScanState.Scanning) return

        setState { copy(scanState = ScanState.Detected(qrData)) }
        scanValidationJob?.cancel()
        scanValidationJob = viewModelScope.launch {
            delay(QR_DETECTION_DELAY_MS)
            setState { copy(scanState = ScanState.Processing(qrData)) }
            delay(QR_VALIDATION_DELAY_MS)

            val otpData = OtpUriParser.parse(qrData)
            if (otpData != null) {
                setState { copy(scanState = ScanState.Success(qrData)) }
                delay(SCAN_SUCCESS_DELAY_MS)
                addAccount(otpData.issuer, otpData.secret, otpData.accountName)
            } else {
                setState { copy(scanState = ScanState.Error("Invalid QR code")) }
                delay(SCAN_ERROR_DISPLAY_MS)
                setState { copy(scanState = ScanState.Scanning) }
            }
        }
    }

    private fun onAddAccount() {
        val state = currentState
        if (state.accountName.isBlank() || state.secretKey.isBlank()) {
            setState { copy(errorMessage = "Please fill in required fields") }
            return
        }
        viewModelScope.launch {
            addAccount(state.accountName, state.secretKey, state.userName)
        }
    }

    private suspend fun addAccount(issuer: String, secret: String, name: String) {
        setState { copy(isLoading = true, errorMessage = null) }
        try {
            val account = Account(
                issuer = issuer,
                name = name.ifBlank { "$issuer User" },
                secret = secret,
                color = getColorForIssuer(issuer),
                icon = issuer.firstOrNull()?.uppercase() ?: "?"
            )
            addAccountUseCase(account)
            setState { copy(isLoading = false, isSuccess = true) }
            setEffect(AddAccountEffect.ShowSnackbar("Account added successfully"))
            delay(NAVIGATE_BACK_DELAY_MS)
            setEffect(AddAccountEffect.NavigateBack)
        } catch (e: Exception) {
            setState { copy(isLoading = false, errorMessage = "Failed to add account") }
        }
    }

    private fun getColorForIssuer(issuer: String): String {
        return when (issuer.lowercase()) {
            "google" -> "#4285F4"
            "github" -> "#333333"
            "microsoft" -> "#00A4EF"
            "dropbox" -> "#0061FF"
            "slack" -> "#4A154B"
            "aws", "amazon" -> "#FF9900"
            else -> generateRandomColor()
        }
    }

    private fun generateRandomColor(): String {
        val hue = Random.nextInt(360)
        return String.format(
            "#%06X",
            android.graphics.Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 0.6f))
        )
    }

    override fun onCleared() {
        scanValidationJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val QR_DETECTION_DELAY_MS = 600L
        private const val QR_VALIDATION_DELAY_MS = 800L
        private const val SCAN_SUCCESS_DELAY_MS = 500L
        private const val SCAN_ERROR_DISPLAY_MS = 1_500L
        private const val NAVIGATE_BACK_DELAY_MS = 1_000L
    }
}
