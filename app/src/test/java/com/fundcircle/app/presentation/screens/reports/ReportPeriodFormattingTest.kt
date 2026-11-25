package com.fundcircle.app.presentation.screens.reports

import com.fundcircle.app.domain.model.ReportPeriod
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class ReportPeriodFormattingTest {

    @Test
    fun periodOptions_spanFromCircleCreationYearToCurrentMonth_newestFirst_noFutureMonths() {
        val options = reportPeriodOptions(earliestYear = 2025, currentYear = 2026, currentMonth = 3)

        assertThat(options.first()).isEqualTo(3 to 2026)
        assertThat(options.last()).isEqualTo(1 to 2025)
        assertThat(options).hasSize(3 + 12)
        assertThat(options).doesNotContain(4 to 2026)
    }

    @Test
    fun periodOptions_areNotCappedToAFixedYearRange() {
        val options = reportPeriodOptions(earliestYear = 2028, currentYear = 2030, currentMonth = 1)

        assertThat(options.first()).isEqualTo(1 to 2030)
        assertThat(options.last()).isEqualTo(1 to 2028)
    }

    @Test
    fun periodOptions_whenCreationYearIsInTheFuture_fallBackToCurrentYear() {
        val options = reportPeriodOptions(earliestYear = 2031, currentYear = 2026, currentMonth = 2)

        assertThat(options).containsExactly(2 to 2026, 1 to 2026).inOrder()
    }

    @Test
    fun shortMonth_followsLocale_andIsNullForAllTime() {
        assertThat(periodShortMonth(ReportPeriod.MonthYear(11, 2026), Locale.US)).isEqualTo("Nov")
        assertThat(periodShortMonth(ReportPeriod.MonthYear(11, 2026), Locale.FRANCE)).isNotEqualTo("Nov")
        assertThat(periodShortMonth(ReportPeriod.AllTime, Locale.US)).isNull()
    }

    @Test
    fun longLabel_usesResourceForAllTime_andLocaleMonthOtherwise() {
        assertThat(periodLongLabel(ReportPeriod.AllTime, Locale.US, "Tout")).isEqualTo("Tout")
        assertThat(periodLongLabel(ReportPeriod.MonthYear(2, 2026), Locale.US, "x")).isEqualTo("February 2026")
    }
}
