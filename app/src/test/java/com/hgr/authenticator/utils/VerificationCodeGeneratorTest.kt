package com.hgr.authenticator.utils

import org.junit.Test
import org.junit.Assert.*

class VerificationCodeGeneratorTest {

    @Test
    fun `generate returns 6 digit code`() {
        val generator = VerificationCodeGenerator("JBSWY3DPEHPK3PXP")
        val code = generator.generate()

        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun `generate returns consistent code within same time window`() {
        val generator = VerificationCodeGenerator("JBSWY3DPEHPK3PXP")
        val code1 = generator.generate()
        val code2 = generator.generate()

        assertEquals(code1, code2)
    }

    @Test
    fun `getTimeRemaining returns value between 0 and 30`() {
        val generator = VerificationCodeGenerator("JBSWY3DPEHPK3PXP")
        val remaining = generator.getTimeRemaining()

        assertTrue(remaining in 0..30)
    }

    @Test
    fun `different secrets produce different codes`() {
        val generator1 = VerificationCodeGenerator("JBSWY3DPEHPK3PXP")
        val generator2 = VerificationCodeGenerator("MZXW6YTBOJUWU23M")

        val code1 = generator1.generate()
        val code2 = generator2.generate()

        assertNotEquals(code1, code2)
    }

    @Test
    fun `code changes after period elapsed`() {
        val secret = "JBSWY3DPEHPK3PXP"
        val generator = VerificationCodeGenerator(secret, period = 1)

        val code1 = generator.generate()
        Thread.sleep(1100)
        val code2 = generator.generate()

        assertNotEquals(code1, code2)
    }
}
