package com.hgr.authenticator.presentation.home

import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.domain.usecase.AddAccountUseCase
import com.hgr.authenticator.domain.usecase.DeleteAccountUseCase
import com.hgr.authenticator.domain.usecase.GenerateVerificationCodeUseCase
import com.hgr.authenticator.domain.usecase.GetAccountsUseCase
import com.hgr.authenticator.domain.usecase.SearchAccountsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel
    private lateinit var getAccountsUseCase: GetAccountsUseCase
    private lateinit var searchAccountsUseCase: SearchAccountsUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var generateVerificationCodeUseCase: GenerateVerificationCodeUseCase

    private val testAccounts = listOf(
        Account(id = 1, issuer = "Google", name = "user@gmail.com", secret = "secret1", color = "#4285F4", icon = "G"),
        Account(id = 2, issuer = "GitHub", name = "dev_user", secret = "secret2", color = "#333333", icon = "H")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getAccountsUseCase = mock(GetAccountsUseCase::class.java)
        searchAccountsUseCase = mock(SearchAccountsUseCase::class.java)
        deleteAccountUseCase = mock(DeleteAccountUseCase::class.java)
        generateVerificationCodeUseCase = mock(GenerateVerificationCodeUseCase::class.java)

        `when`(getAccountsUseCase.invoke()).thenReturn(flowOf(testAccounts))
        `when`(generateVerificationCodeUseCase.invoke("secret1")).thenReturn(
            GenerateVerificationCodeUseCase.VerificationCodeResult("123 456", 15, 0.5f)
        )
        `when`(generateVerificationCodeUseCase.invoke("secret2")).thenReturn(
            GenerateVerificationCodeUseCase.VerificationCodeResult("789 012", 20, 0.67f)
        )

        viewModel = HomeViewModel(
            getAccountsUseCase = getAccountsUseCase,
            searchAccountsUseCase = searchAccountsUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            generateVerificationCodeUseCase = generateVerificationCodeUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads accounts`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.accounts.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onSearchQueryChange filters accounts`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Google")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.accounts.size)
        assertEquals("Google", state.accounts[0].issuer)
    }

    @Test
    fun `onToggleSearch toggles search active state`() = runTest {
        assertFalse(viewModel.uiState.value.isSearchActive)

        viewModel.onToggleSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSearchActive)
    }

    @Test
    fun `onToggleSearch clears query when deactivating`() = runTest {
        viewModel.onToggleSearch()
        viewModel.onSearchQueryChange("test")
        viewModel.onToggleSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `onOpenMenu opens menu`() = runTest {
        viewModel.onOpenMenu()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isMenuOpen)
    }

    @Test
    fun `onCloseMenu closes menu`() = runTest {
        viewModel.onOpenMenu()
        viewModel.onCloseMenu()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isMenuOpen)
    }

    @Test
    fun `onDeleteAccount calls delete use case`() = runTest {
        viewModel.onDeleteAccount(1)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(deleteAccountUseCase).invoke(1)
    }

    @Test
    fun `onCopyCode sets copied account id`() = runTest {
        viewModel.onCopyCode(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1L, viewModel.uiState.value.copiedAccountId)
    }

    @Test
    fun `timer updates verification codes`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = viewModel.uiState.value
        assertEquals(2, initialState.verificationCodeResults.size)
    }
}
