package com.fundcircle.app.data.reports

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure Kotlin RFC 4180 streaming CSV generator with UTF-8 BOM prefix.
 * Zero-server-fee, offline-capable, and compatible with Microsoft Excel and Google Sheets.
 *
 * Invariant 1: Monetary figures formatted strictly as whole-integer Long (no poisha/cents).
 * Invariant 4: Audit trail preserves reversed transactions with original whole-integer amount and void reason.
 * Invariant 5: 100% on-device pure Kotlin with zero external server dependencies.
 */
@Singleton
class LedgerCsvGenerator @Inject constructor() {

    fun generateCsv(
        reportType: ReportType,
        circle: FundCircle,
        monthlyReport: MonthlyFinancialReport? = null,
        members: List<CircleMember> = emptyList(),
        deposits: List<Deposit> = emptyList(),
        expenses: List<Expense> = emptyList(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String = when (reportType) {
        ReportType.MONTHLY_SUMMARY -> generateMonthlySummaryCsv(circle, monthlyReport)
        ReportType.MEMBER_STATEMENT -> generateMemberStatementCsv(members, deposits)
        ReportType.DEPOSIT_HISTORY -> generateDepositHistoryCsv(deposits, members, timeZone)
        ReportType.EXPENSE_HISTORY -> generateExpenseHistoryCsv(expenses, timeZone)
    }

    /**
     * 1. Monthly Financial Summary:
     * Header: Month,Opening Balance,Total Deposits,Total Expenses,Net Change,Closing Balance
     * Data: November 2026,1362000,100000,12000,88000,1450000
     */
    fun generateMonthlySummaryCsv(
        circle: FundCircle,
        monthlyReport: MonthlyFinancialReport?
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.appendLine("Month,Opening Balance,Total Deposits,Total Expenses,Net Change,Closing Balance")

        if (monthlyReport != null) {
            val row = listOf(
                escapeCsv(monthlyReport.periodDisplay),
                monthlyReport.openingBalance.toString(),
                monthlyReport.totalDeposits.toString(),
                monthlyReport.totalExpenses.toString(),
                monthlyReport.netChange.toString(),
                monthlyReport.closingBalance.toString()
            ).joinToString(",")
            sb.appendLine(row)
        } else {
            val row = listOf(
                escapeCsv("All Time"),
                "0",
                circle.totalDeposits.toString(),
                circle.totalExpenses.toString(),
                (circle.totalDeposits - circle.totalExpenses).toString(),
                circle.recordedBalance.toString()
            ).joinToString(",")
            sb.appendLine(row)
        }

        return sb.toString()
    }

    /**
     * 2. Member-wise Deposit Statement:
     * Header: Member ID,Name,Email,Total Contributed,Deposit Count,Status
     * Data: FC-001,"Kawsar Ahmed",kawsar@example.com,300000,3,ACTIVE
     */
    fun generateMemberStatementCsv(
        members: List<CircleMember>,
        deposits: List<Deposit>
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.appendLine("Member ID,Name,Email,Total Contributed,Deposit Count,Status")

        // Filter out voided transactions for contribution counts
        val activeDeposits = deposits.filter { !it.isVoid }

        members.forEach { member ->
            val count = activeDeposits.count { deposit ->
                deposit.memberId == member.memberId ||
                        deposit.memberId == member.userId ||
                        deposit.memberEmail.equals(member.email, ignoreCase = true)
            }
            val row = listOf(
                escapeCsv(member.memberId),
                escapeCsv(member.name),
                escapeCsv(member.email),
                member.totalContributed.toString(),
                count.toString(),
                escapeCsv(member.status.name)
            ).joinToString(",")
            sb.appendLine(row)
        }

        return sb.toString()
    }

    /**
     * 3. Complete Deposit History:
     * Header: Deposit ID,Date,Member ID,Member Name,Member Email,Amount,Payment Method,Note,Status,Void Reason
     */
    fun generateDepositHistoryCsv(
        deposits: List<Deposit>,
        members: List<CircleMember> = emptyList(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.appendLine("Deposit ID,Date,Member ID,Member Name,Member Email,Amount,Payment Method,Note,Status,Void Reason")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            this.timeZone = timeZone
        }

        val memberMap = members.associateBy { it.userId }

        deposits.forEach { deposit ->
            val status = if (deposit.isVoid) "REVERSED" else "POSTED"
            val voidReason = deposit.voidMetadata?.reason.orEmpty()
            val dateStr = dateFormat.format(Date(deposit.depositDate))
            val resolvedMemberId = memberMap[deposit.memberId]?.memberId
                ?: members.firstOrNull { it.email.equals(deposit.memberEmail, ignoreCase = true) }?.memberId
                ?: deposit.memberId

            val row = listOf(
                escapeCsv(deposit.id),
                escapeCsv(dateStr),
                escapeCsv(resolvedMemberId),
                escapeCsv(deposit.memberName),
                escapeCsv(deposit.memberEmail),
                deposit.amount.toString(),
                escapeCsv(deposit.paymentMethod.name),
                escapeCsv(deposit.note.orEmpty()),
                escapeCsv(status),
                escapeCsv(voidReason)
            ).joinToString(",")
            sb.appendLine(row)
        }

        return sb.toString()
    }

    /**
     * 4. Complete Expense History:
     * Header: Expense ID,Date,Category,Description,Amount,Payment Method,Paid By,Creator Email,Status,Void Reason
     */
    fun generateExpenseHistoryCsv(
        expenses: List<Expense>,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.appendLine("Expense ID,Date,Category,Description,Amount,Payment Method,Paid By,Creator Email,Status,Void Reason")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            this.timeZone = timeZone
        }

        expenses.forEach { expense ->
            val status = if (expense.isVoid) "REVERSED" else "POSTED"
            val voidReason = expense.voidMetadata?.reason.orEmpty()
            val dateStr = dateFormat.format(Date(expense.expenseDate))

            val row = listOf(
                escapeCsv(expense.id),
                escapeCsv(dateStr),
                escapeCsv(expense.category.name),
                escapeCsv(expense.description),
                expense.amount.toString(),
                escapeCsv(expense.paymentMethod.name),
                escapeCsv(expense.paidBy),
                escapeCsv(expense.creatorEmail),
                escapeCsv(status),
                escapeCsv(voidReason)
            ).joinToString(",")
            sb.appendLine(row)
        }

        return sb.toString()
    }

    /**
     * Escapes one free-text field for RFC 4180 and against spreadsheet formula injection.
     *
     * - A field starting with `=`, `+`, `-`, `@`, tab or carriage return would run as a formula in
     *   Excel or Google Sheets, so it is prefixed with `'` (shown as text, never evaluated) and quoted.
     * - A field containing a comma, quote or line break is enclosed in double quotes, with each
     *   inner double quote doubled ("").
     *
     * Numeric amount columns are written directly and never pass through here, so a negative
     * net change still reads as a number.
     */
    fun escapeCsv(value: String): String {
        val isFormula = value.isNotEmpty() && value[0] in FORMULA_TRIGGERS
        val safeValue = if (isFormula) "'$value" else value

        val containsSpecial = isFormula ||
                safeValue.contains(',') ||
                safeValue.contains('"') ||
                safeValue.contains('\n') ||
                safeValue.contains('\r')

        return if (containsSpecial) {
            "\"${safeValue.replace("\"", "\"\"")}\""
        } else {
            safeValue
        }
    }

    companion object {
        const val UTF8_BOM = "\uFEFF"
        private val FORMULA_TRIGGERS = setOf('=', '+', '-', '@', '\t', '\r')
    }
}
