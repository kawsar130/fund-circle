package com.fundcircle.app

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SanityUnitTest {

    @Test
    fun baselineUnitTestExecution_succeeds() {
        val appName = "Fund Circle"
        assertThat(appName).isEqualTo("Fund Circle")
    }

    @Test
    fun invariant1_wholeIntegerMonetaryModel_is64BitLong() {
        val wholeIntegerDeposit: Long = 50000L
        assertThat(wholeIntegerDeposit).isAtLeast(1L)
        assertThat(wholeIntegerDeposit % 1L).isEqualTo(0L)
    }
}
