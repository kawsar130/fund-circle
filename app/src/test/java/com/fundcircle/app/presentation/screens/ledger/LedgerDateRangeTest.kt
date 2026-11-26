package com.fundcircle.app.presentation.screens.ledger

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class LedgerDateRangeTest {

    private val dhaka = TimeZone.getTimeZone("Asia/Dhaka") // UTC+6
    private val losAngeles = TimeZone.getTimeZone("America/Los_Angeles") // UTC-8 in November

    /** What the M3 DateRangePicker returns for a calendar day: UTC midnight. */
    private fun pickerDay(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis

    private fun localTime(tz: TimeZone, year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(tz).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis

    private val nov10to12 = LedgerDateRange(
        startUtcDay = pickerDay(2026, Calendar.NOVEMBER, 10),
        endUtcDay = pickerDay(2026, Calendar.NOVEMBER, 12)
    )

    @Test
    fun includesBothEndDaysCompletely() {
        assertThat(nov10to12.contains(localTime(dhaka, 2026, Calendar.NOVEMBER, 10, 0, 0), dhaka)).isTrue()
        assertThat(nov10to12.contains(localTime(dhaka, 2026, Calendar.NOVEMBER, 12, 23, 59), dhaka)).isTrue()
    }

    @Test
    fun excludesTheDaysJustOutside() {
        assertThat(nov10to12.contains(localTime(dhaka, 2026, Calendar.NOVEMBER, 9, 23, 59), dhaka)).isFalse()
        assertThat(nov10to12.contains(localTime(dhaka, 2026, Calendar.NOVEMBER, 13, 0, 0), dhaka)).isFalse()
    }

    @Test
    fun earlyMorningAheadOfUtc_staysOnItsLocalDay() {
        // 13 Nov 02:00 in Dhaka is still 12 Nov in UTC, but the user sees it as 13 Nov.
        val earlyNov13 = localTime(dhaka, 2026, Calendar.NOVEMBER, 13, 2, 0)

        assertThat(nov10to12.contains(earlyNov13, dhaka)).isFalse()
    }

    @Test
    fun lateEveningBehindUtc_staysOnItsLocalDay() {
        // 12 Nov 22:00 in Los Angeles is already 13 Nov in UTC, but the user sees it as 12 Nov.
        val lateNov12 = localTime(losAngeles, 2026, Calendar.NOVEMBER, 12, 22, 0)

        assertThat(nov10to12.contains(lateNov12, losAngeles)).isTrue()
    }

    @Test
    fun singleDayRange_matchesOnlyThatDay() {
        val nov10 = pickerDay(2026, Calendar.NOVEMBER, 10)
        val oneDay = LedgerDateRange(nov10, nov10)

        assertThat(oneDay.contains(localTime(dhaka, 2026, Calendar.NOVEMBER, 10, 12, 0), dhaka)).isTrue()
        assertThat(oneDay.contains(localTime(dhaka, 2026, Calendar.NOVEMBER, 11, 12, 0), dhaka)).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAStartAfterTheEnd() {
        LedgerDateRange(
            startUtcDay = pickerDay(2026, Calendar.NOVEMBER, 12),
            endUtcDay = pickerDay(2026, Calendar.NOVEMBER, 10)
        )
    }
}
