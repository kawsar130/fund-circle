package com.fundcircle.app.domain.usecase

import com.fundcircle.app.data.reports.LedgerCsvGenerator
import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportType
import javax.inject.Inject

/**
 * UseCase for generating RFC 4180 streaming CSV reports from on-device ledger data.
 */
class GenerateReportCsvUseCase @Inject constructor(
    private val csvGenerator: LedgerCsvGenerator
) {

    operator fun invoke(
        reportType: ReportType,
        circle: FundCircle,
        monthlyReport: MonthlyFinancialReport? = null,
        members: List<CircleMember> = emptyList(),
        deposits: List<Deposit> = emptyList(),
        expenses: List<Expense> = emptyList()
    ): String {
        return csvGenerator.generateCsv(
            reportType = reportType,
            circle = circle,
            monthlyReport = monthlyReport,
            members = members,
            deposits = deposits,
            expenses = expenses
        )
    }
}
