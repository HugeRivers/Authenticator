package com.hgr.authenticator.presentation.home

import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.usecase.GenerateVerificationCodeUseCase
import com.hgr.authenticator.presentation.base.UiEffect
import com.hgr.authenticator.presentation.base.UiEvent
import com.hgr.authenticator.presentation.base.UiState

/**
 * 首页 MVI 契约。
 *
 * 集中声明首页的 State、Event、Effect，确保三层职责清晰。
 */
object HomeContract {

    /**
     * 首页 UI 状态。
     */
    data class HomeState(
        val accounts: List<Account> = emptyList(),
        val verificationCodeResults: Map<Long, GenerateVerificationCodeUseCase.VerificationCodeResult> = emptyMap(),
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val isMenuOpen: Boolean = false,
        val isLoading: Boolean = true,
        val copiedAccountId: Long? = null,
        val hasTimeDrift: Boolean = false,
        val deleteConfirmAccountId: Long? = null,
        val detailAccountId: Long? = null,
        val showAboutDialog: Boolean = false
    ) : UiState

    /**
     * 首页 UI 事件。
     */
    sealed class HomeEvent : UiEvent {
        data class OnSearchQueryChange(val query: String) : HomeEvent()
        data object OnToggleSearch : HomeEvent()
        data object OnOpenMenu : HomeEvent()
        data object OnCloseMenu : HomeEvent()
        data class OnCopyCode(val accountId: Long) : HomeEvent()
        data class OnDeleteAccount(val id: Long) : HomeEvent()
        data class OnDeleteConfirmed(val id: Long) : HomeEvent()
        data object OnDeleteDismissed : HomeEvent()
        data class OnShowAccountDetail(val id: Long) : HomeEvent()
        data object OnDismissDetail : HomeEvent()
        data object OnShowAbout : HomeEvent()
        data object OnDismissAbout : HomeEvent()
        data object OnTimeDriftWarningClick : HomeEvent()
        data object OnRefreshCodes : HomeEvent()
    }

    /**
     * 首页 UI 副作用。
     */
    sealed class HomeEffect : UiEffect {
        data class ShowSnackbar(val message: String) : HomeEffect()
        data object NavigateToSettings : HomeEffect()
        data object NavigateToDateSettings : HomeEffect()
    }
}
