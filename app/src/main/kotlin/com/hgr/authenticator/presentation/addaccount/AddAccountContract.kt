package com.hgr.authenticator.presentation.addaccount

import com.hgr.authenticator.presentation.base.UiEffect
import com.hgr.authenticator.presentation.base.UiEvent
import com.hgr.authenticator.presentation.base.UiState

/**
 * 添加账户页 MVI 契约。
 */
object AddAccountContract {

    /**
     * 添加账户页 UI 状态。
     */
    data class AddAccountState(
        val selectedTab: Tab = Tab.Scan,
        val accountName: String = "",
        val secretKey: String = "",
        val userName: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false,
        val scanState: ScanState = ScanState.Idle,
        val hasCameraPermission: Boolean = false
    ) : UiState

    enum class Tab { Scan, Manual }

    /**
     * 扫码状态机。
     */
    sealed class ScanState {
        data object Idle : ScanState()
        data object Scanning : ScanState()
        data class Detected(val qrData: String) : ScanState()
        data class Processing(val qrData: String) : ScanState()
        data class Success(val qrData: String) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    /**
     * 添加账户页 UI 事件。
     */
    sealed class AddAccountEvent : UiEvent {
        data class OnTabSelected(val tab: Tab) : AddAccountEvent()
        data class OnAccountNameChange(val name: String) : AddAccountEvent()
        data class OnSecretKeyChange(val key: String) : AddAccountEvent()
        data class OnUserNameChange(val name: String) : AddAccountEvent()
        data object OnAddAccount : AddAccountEvent()
        data class OnCameraPermissionResult(val granted: Boolean) : AddAccountEvent()
        data object OnCameraPermissionRequested : AddAccountEvent()
        data class OnQrCodeDetected(val qrData: String) : AddAccountEvent()
        data object OnDismissError : AddAccountEvent()
    }

    /**
     * 添加账户页 UI 副作用。
     */
    sealed class AddAccountEffect : UiEffect {
        data class ShowSnackbar(val message: String) : AddAccountEffect()
        data object NavigateBack : AddAccountEffect()
    }
}
