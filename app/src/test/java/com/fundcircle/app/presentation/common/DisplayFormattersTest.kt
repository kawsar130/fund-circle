package com.fundcircle.app.presentation.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DisplayFormattersTest {

    @Test
    fun initials_takeFirstLetterOfFirstTwoWords_uppercased() {
        assertThat(extractInitials("Kawsar Ahmed")).isEqualTo("KA")
        assertThat(extractInitials("  john   doe smith ")).isEqualTo("JD")
    }

    @Test
    fun initials_forSingleWord_takeFirstTwoLetters() {
        assertThat(extractInitials("Apex")).isEqualTo("AP")
        assertThat(extractInitials("A")).isEqualTo("A")
    }

    @Test
    fun initials_forBlankName_fallBackToMember() {
        assertThat(extractInitials("")).isEqualTo("M")
        assertThat(extractInitials("   ")).isEqualTo("M")
    }

    @Test
    fun shortRef_usesTypePrefixAndLastFourOfUuid() {
        assertThat(shortTransactionRef("9728062f-9358-482b-b747-06394348633b", isExpense = false))
            .isEqualTo("TX-DEP-633B")
        assertThat(shortTransactionRef("c1234567-89ab-cdef-0123-456789abcdef", isExpense = true))
            .isEqualTo("TX-EXP-CDEF")
    }

    @Test
    fun shortRef_leavesAlreadyFormattedIdsUnchanged() {
        assertThat(shortTransactionRef("TX-DEP-9821", isExpense = true)).isEqualTo("TX-DEP-9821")
    }
}
