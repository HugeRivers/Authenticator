package com.hgr.authenticator.domain.usecase

import org.junit.Test
import org.junit.Assert.*

class GenerateVerificationCodeUseCaseTest {

    private val useCase = GenerateVerificationCodeUseCase()

    @Test
    fun `invoke returns formatted code with space`() {
        val result = useCase.invoke("JBSWY3DPEHPK3PXP")

        assertEquals(7, result.code.length)
        assertEquals(' ', result.code[3])
        assertTrue(result.code.replace(" ", "").all { it.isDigit() })
    }

    @Test
    fun `invoke returns time remaining within valid range`() {
        val result = useCase.invoke("JBSWY3DPEHPK3PXP")

        assertTrue(result.timeRemaining in 0..30)
    }

    @Test
    fun `invoke returns progress between 0 and 1`() {
        val result = useCase.invoke("JBSWY3DPEHPK3PXP")

        assertTrue(result.progress in 0f..1f)
    }

    @Test
    fun `invoke with custom period uses correct period`() {
        val result = useCase.invoke("JBSWY3DPEHPK3PXP", period = 60)

        assertTrue(result.timeRemaining in 0..60)
    }
}
