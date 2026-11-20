package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.ReportPeriod
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GenerateMonthlyReportUseCaseTest {

    private lateinit var useCase: GenerateMonthlyReportUseCase

    private val circle = FundCircle(
        id = "circle-1",
        name = "Green Valley Property Pool",
        currency = Currency.BDT,
        description = "Investment circle",
        adminId = "user-1",
        adminEmail = "admin@example.com",
        memberEmails = listOf("admin@example.com"),
        memberCount = 1,
        totalDeposits = 2000000L,
        totalExpenses = 200000L,
        recordedBalance = 1800000L,
        isLocked = false,
        createdAt = 1700000000000L
    )

    @Before
    fun setUp() {
        useCase = GenerateMonthlyReportUseCase()
    }

    @Test
    fun `invoke calculates opening balance, monthly totals, net change and closing balance accurately`() {
        // Month: November 2026 (month = 11, year = 2026)
        val cal = Calendar.getInstance()
        cal.clear()

        // October 2026 (Prior to November 2026)
        cal.set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        val octTimestamp = cal.timeInMillis

        // November 2026
        cal.set(2026, Calendar.NOVEMBER, 10, 12, 0, 0)
        val novTimestamp1 = cal.timeInMillis
        cal.set(2026, Calendar.NOVEMBER, 20, 14, 0, 0)
        val novTimestamp2 = cal.timeInMillis

        val deposits = listOf(
            Deposit(
                id = "dep-oct",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "admin@example.com",
                memberName = "Admin",
                amount = 1000000L,
                depositDate = octTimestamp,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null
            ),
            Deposit(
                id = "dep-nov-1",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "admin@example.com",
                memberName = "Admin",
                amount = 500000L,
                depositDate = novTimestamp1,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null
            ),
            Deposit(
                id = "dep-nov-void",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "admin@example.com",
                memberName = "Admin",
                amount = 200000L,
                depositDate = novTimestamp2,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null,
                isVoid = true // Reversed transaction must NOT be counted
            )
        )

        val expenses = listOf(
            Expense(
                id = "exp-oct",
                fundId = "circle-1",
                amount = 50000L,
                expenseDate = octTimestamp,
                category = ExpenseCategory.LEGAL_AND_DOCUMENTATION,
                description = "Legal deed",
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                paidBy = "Admin",
                createdBy = "user-1",
                creatorEmail = "admin@example.com"
            ),
            Expense(
                id = "exp-nov-1",
                fundId = "circle-1",
                amount = 20000L,
                expenseDate = novTimestamp1,
                category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
                description = "Printing",
                paymentMethod = PaymentMethod.CASH,
                paidBy = "Admin",
                createdBy = "user-1",
                creatorEmail = "admin@example.com"
            )
        )

        val report = useCase(
            circle = circle,
            deposits = deposits,
            expenses = expenses,
            period = ReportPeriod.MonthYear(11, 2026)
        )

        // Prior deposits = 1,000,000, Prior expenses = 50,000 => Opening Balance = 950,000
        assertThat(report.openingBalance).isEqualTo(950000L)
        // November active deposits = 500,000 (excluding 200,000 voided)
        assertThat(report.totalDeposits).isEqualTo(500000L)
        // November active expenses = 20,000
        assertThat(report.totalExpenses).isEqualTo(200000L.let { 20000L })
        // Net change = 500,000 - 20,000 = +480,000
        assertThat(report.netChange).isEqualTo(480000L)
        // Closing balance = 950,000 + 480,000 = 1,430,000
        assertThat(report.closingBalance).isEqualTo(1430000L)
        assertThat(report.periodDisplay).isEqualTo("November 2026")
    }

    @Test
    fun `invoke with zero transactions in target month maintains opening balance as closing balance`() {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(2026, Calendar.JANUARY, 10, 12, 0, 0)
        val janTimestamp = cal.timeInMillis

        val deposits = listOf(
            Deposit(
                id = "dep-jan",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "admin@example.com",
                memberName = "Admin",
                amount = 500000L,
                depositDate = janTimestamp,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null
            )
        )

        // Querying for June 2026 where zero transactions exist
        val report = useCase(
            circle = circle,
            deposits = deposits,
            expenses = emptyList(),
            period = ReportPeriod.MonthYear(6, 2026)
        )

        assertThat(report.openingBalance).isEqualTo(500000L)
        assertThat(report.totalDeposits).isEqualTo(0L)
        assertThat(report.totalExpenses).isEqualTo(0L)
        assertThat(report.netChange).isEqualTo(0L)
        assertThat(report.closingBalance).isEqualTo(500000L)
        assertThat(report.transactionCount).isEqualTo(0)
    }

    @Test
    fun `invoke with AllTime period computes cumulative active ledger balance`() {
        val deposits = listOf(
            Deposit(
                id = "dep-1",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "admin@example.com",
                memberName = "Admin",
                amount = 1000000L,
                depositDate = 1700000000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null
            ),
            Deposit(
                id = "dep-2",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "admin@example.com",
                memberName = "Admin",
                amount = 500000L,
                depositDate = 1700010000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null,
                isVoid = true
            )
        )

        val expenses = listOf(
            Expense(
                id = "exp-1",
                fundId = "circle-1",
                amount = 100000L,
                expenseDate = 1700020000000L,
                category = ExpenseCategory.LEGAL_AND_DOCUMENTATION,
                description = "Legal",
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                paidBy = "Admin",
                createdBy = "user-1",
                creatorEmail = "admin@example.com"
            )
        )

        val report = useCase(
            circle = circle,
            deposits = deposits,
            expenses = expenses,
            period = ReportPeriod.AllTime
        )

        assertThat(report.openingBalance).isEqualTo(0L)
        assertThat(report.totalDeposits).isEqualTo(1000000L)
        assertThat(report.totalExpenses).isEqualTo(100000L)
        assertThat(report.netChange).isEqualTo(900000L)
        assertThat(report.closingBalance).isEqualTo(900000L)
        assertThat(report.periodDisplay).isEqualTo("All Time")
    }
}
