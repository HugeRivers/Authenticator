package com.hgr.authenticator.data.repository

import com.hgr.authenticator.data.local.AccountDao
import com.hgr.authenticator.data.local.AccountEntity
import com.hgr.authenticator.domain.model.Account
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AccountRepositoryImplTest {

    private lateinit var repository: AccountRepositoryImpl
    private val accountDao: AccountDao = mock(AccountDao::class.java)

    private val testEntity = AccountEntity(
        id = 1,
        issuer = "Google",
        name = "user@gmail.com",
        secret = "secret",
        color = "#4285F4",
        icon = "G"
    )

    @Before
    fun setup() {
        repository = AccountRepositoryImpl(accountDao)
    }

    @Test
    fun `getAllAccounts returns mapped domain models`() = runTest {
        `when`(accountDao.getAll()).thenReturn(flowOf(listOf(testEntity)))

        val result = repository.getAllAccounts().first()

        assertEquals(1, result.size)
        assertEquals("Google", result[0].issuer)
        assertEquals("user@gmail.com", result[0].name)
    }

    @Test
    fun `searchAccounts returns filtered results`() = runTest {
        `when`(accountDao.search("Google")).thenReturn(flowOf(listOf(testEntity)))

        val result = repository.searchAccounts("Google").first()

        assertEquals(1, result.size)
        assertEquals("Google", result[0].issuer)
    }

    @Test
    fun `insertAccount calls dao insert`() = runTest {
        val account = Account(
            issuer = "GitHub",
            name = "dev",
            secret = "secret",
            color = "#333333",
            icon = "H"
        )

        repository.insertAccount(account)

        verify(accountDao).insert(org.mockito.kotlin.any())
    }

    @Test
    fun `deleteAccount calls dao delete`() = runTest {
        repository.deleteAccount(1)

        verify(accountDao).delete(1)
    }
}
