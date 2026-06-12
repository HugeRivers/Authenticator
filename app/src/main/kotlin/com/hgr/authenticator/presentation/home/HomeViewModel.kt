package com.hgr.authenticator.presentation.home

import androidx.lifecycle.viewModelScope
import com.hgr.authenticator.domain.usecase.DeleteAccountUseCase
import com.hgr.authenticator.domain.usecase.GenerateVerificationCodeUseCase
import com.hgr.authenticator.domain.usecase.GetAccountsUseCase
import com.hgr.authenticator.presentation.base.BaseViewModel
import com.hgr.authenticator.presentation.home.HomeContract.HomeEffect
import com.hgr.authenticator.presentation.home.HomeContract.HomeEvent
import com.hgr.authenticator.presentation.home.HomeContract.HomeState
import com.hgr.authenticator.utils.TimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 ViewModel。
 *
 * 负责管理账户列表、TOTP 验证码刷新、搜索、删除、复制等核心业务逻辑。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    getAccountsUseCase: GetAccountsUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val generateVerificationCodeUseCase: GenerateVerificationCodeUseCase
) : BaseViewModel<HomeState, HomeEvent, HomeEffect>(HomeState()) {

    private val accountsFlow = getAccountsUseCase()
    private var codeRefreshJob: Job? = null
    private var copyTimeoutJob: Job? = null

    init {
        observeAccounts()
        startVerificationCodeTimer()
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnSearchQueryChange -> onSearchQueryChange(event.query)
            is HomeEvent.OnToggleSearch -> onToggleSearch()
            is HomeEvent.OnOpenMenu -> setState { copy(isMenuOpen = true) }
            is HomeEvent.OnCloseMenu -> setState { copy(isMenuOpen = false) }
            is HomeEvent.OnCopyCode -> onCopyCode(event.accountId)
            is HomeEvent.OnDeleteAccount -> setState { copy(deleteConfirmAccountId = event.id) }
            is HomeEvent.OnDeleteConfirmed -> onDeleteConfirmed(event.id)
            is HomeEvent.OnDeleteDismissed -> setState { copy(deleteConfirmAccountId = null) }
            is HomeEvent.OnShowAccountDetail -> setState { copy(detailAccountId = event.id) }
            is HomeEvent.OnDismissDetail -> setState { copy(detailAccountId = null) }
            is HomeEvent.OnShowAbout -> setState { copy(showAboutDialog = true) }
            is HomeEvent.OnDismissAbout -> setState { copy(showAboutDialog = false) }
            is HomeEvent.OnTimeDriftWarningClick -> setEffect(HomeEffect.NavigateToDateSettings)
            is HomeEvent.OnRefreshCodes -> refreshVerificationCodes()
        }
    }

    private fun onSearchQueryChange(query: String) {
        setState { copy(searchQuery = query) }
    }

    private fun onToggleSearch() {
        setState {
            copy(
                isSearchActive = !isSearchActive,
                searchQuery = if (isSearchActive) "" else searchQuery
            )
        }
    }

    private fun onCopyCode(accountId: Long) {
        setState { copy(copiedAccountId = accountId) }
        copyTimeoutJob?.cancel()
        copyTimeoutJob = viewModelScope.launch {
            delay(COPY_TIMEOUT_MS)
            setState { copy(copiedAccountId = null) }
        }
    }

    private fun onDeleteConfirmed(id: Long) {
        setState { copy(deleteConfirmAccountId = null) }
        viewModelScope.launch {
            deleteAccountUseCase(id)
        }
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            accountsFlow.collect { accounts ->
                setState { copy(accounts = accounts, isLoading = false) }
            }
        }
    }

    private fun startVerificationCodeTimer() {
        codeRefreshJob?.cancel()
        codeRefreshJob = viewModelScope.launch {
            while (true) {
                refreshVerificationCodes()
                setState { copy(hasTimeDrift = TimeSource.hasSignificantDrift()) }
                delay(VERIFICATION_CODE_REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun refreshVerificationCodes() {
        val results = currentState.accounts.associate { account ->
            account.id to generateVerificationCodeUseCase(account.secret, account.period)
        }
        setState { copy(verificationCodeResults = results) }
    }

    override fun onCleared() {
        codeRefreshJob?.cancel()
        copyTimeoutJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val VERIFICATION_CODE_REFRESH_INTERVAL_MS = 1_000L
        private const val COPY_TIMEOUT_MS = 2_000L
    }
}
