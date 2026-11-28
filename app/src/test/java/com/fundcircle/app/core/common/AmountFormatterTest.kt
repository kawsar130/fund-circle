package com.fundcircle.app.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class AmountFormatterTest {

    @Test
    fun groupsThousandsWithCommas() {
        assertThat(AmountFormatter.format(1_450_000L)).isEqualTo("1,450,000")
        assertThat(AmountFormatter.format(3_500L)).isEqualTo("3,500")
    }

    @Test
    fun leavesSmallAmountsUngrouped() {
        assertThat(AmountFormatter.format(1L)).isEqualTo("1")
        assertThat(AmountFormatter.format(999L)).isEqualTo("999")
    }

    @Test
    fun keepsTheMinusSignForNegativeValues() {
        assertThat(AmountFormatter.format(-25_000L)).isEqualTo("-25,000")
    }

    @Test
    fun handlesTheLargestLongWithoutOverflow() {
        assertThat(AmountFormatter.format(Long.MAX_VALUE)).isEqualTo("9,223,372,036,854,775,807")
    }

    @Test
    fun ignoresTheDeviceLocale() {
        val original = Locale.getDefault()
        try {
            // German and Bengali devices would otherwise use "." grouping or Bengali digits.
            Locale.setDefault(Locale.GERMANY)
            assertThat(AmountFormatter.format(1_450_000L)).isEqualTo("1,450,000")
            Locale.setDefault(Locale.forLanguageTag("bn-BD"))
            assertThat(AmountFormatter.format(1_450_000L)).isEqualTo("1,450,000")
        } finally {
            Locale.setDefault(original)
        }
    }
}
