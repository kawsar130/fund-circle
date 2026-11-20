package com.fundcircle.app.domain.model

/**
 * Period filter for generating financial reports.
 */
sealed interface ReportPeriod {
    data class MonthYear(val month: Int, val year: Int) : ReportPeriod
    data object AllTime : ReportPeriod
}
