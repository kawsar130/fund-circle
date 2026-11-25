package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportPeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * UseCase for computing statement aggregates for Screen 12 live preview and monthly summary export.
 * Invariant 1: All financial values are 64-bit Long integers. No Double/Float.
 */
class GenerateMonthlyReportUseCase @Inject constructor() {

    operator fun invoke(
        circle: FundCircle,
        deposits: List<Deposit>,
        expenses: List<Expense>,
        period: ReportPeriod,
        timeZone: TimeZone = TimeZone.getDefault()
    ): MonthlyFinancialReport {
        return when (period) {
            is ReportPeriod.MonthYear -> {
                val calendar = Calendar.getInstance(timeZone)
                calendar.clear()
                calendar.set(period.year, period.month - 1, 1, 0, 0, 0)
                val startMs = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endMs = calendar.timeInMillis

                val priorDeposits = deposits
                    .filter { !it.isVoid && it.depositDate < startMs }
                    .sumOf { it.amount }
                val priorExpenses = expenses
                    .filter { !it.isVoid && it.expenseDate < startMs }
                    .sumOf { it.amount }
                val openingBalance = priorDeposits - priorExpenses

                val periodDeposits = deposits
                    .filter { !it.isVoid && it.depositDate in startMs..endMs }
                    .sumOf { it.amount }
                val periodExpenses = expenses
                    .filter { !it.isVoid && it.expenseDate in startMs..endMs }
                    .sumOf { it.amount }

                val netChange = periodDeposits - periodExpenses
                val closingBalance = openingBalance + netChange
                val txCount = deposits.count { it.depositDate in startMs..endMs } +
                        expenses.count { it.expenseDate in startMs..endMs }

                val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.US).apply {
                    this.timeZone = timeZone
                }
                val periodDisplay = monthFormatter.format(Date(startMs))

                MonthlyFinancialReport(
                    circleName = circle.name,
                    currency = circle.currency,
                    periodDisplay = periodDisplay,
                    openingBalance = openingBalance,
                    totalDeposits = periodDeposits,
                    totalExpenses = periodExpenses,
                    netChange = netChange,
                    closingBalance = closingBalance,
                    transactionCount = txCount,
                    period = period
                )
            }
            is ReportPeriod.AllTime -> {
                val totalDeposits = deposits
                    .filter { !it.isVoid }
                    .sumOf { it.amount }
                val totalExpenses = expenses
                    .filter { !it.isVoid }
                    .sumOf { it.amount }
                val netChange = totalDeposits - totalExpenses

                MonthlyFinancialReport(
                    circleName = circle.name,
                    currency = circle.currency,
                    periodDisplay = "All Time",
                    openingBalance = 0L,
                    totalDeposits = totalDeposits,
                    totalExpenses = totalExpenses,
                    netChange = netChange,
                    closingBalance = netChange,
                    transactionCount = deposits.size + expenses.size,
                    period = ReportPeriod.AllTime
                )
            }
        }
    }
}
