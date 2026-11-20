package com.fundcircle.app.data.reports

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.ReportType
import com.fundcircle.app.domain.model.VoidMetadata
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class LedgerCsvGeneratorTest {

    private lateinit var generator: LedgerCsvGenerator

    private val sampleCircle = FundCircle(
        id = "circle-1",
        name = "Green Valley Property, LLC",
        currency = Currency.BDT,
        description = "Investment circle",
        adminId = "user-1",
        adminEmail = "admin@example.com",
        memberEmails = listOf("admin@example.com", "member@example.com"),
        memberCount = 2,
        totalDeposits = 1450000L,
        totalExpenses = 88000L,
        recordedBalance = 1362000L,
        isLocked = false,
        createdAt = 1700000000000L
    )

    @Before
    fun setUp() {
        generator = LedgerCsvGenerator()
    }

    @Test
    fun `generateMonthlySummaryCsv prepends UTF-8 BOM and outputs valid RFC 4180 format`() {
        val report = MonthlyFinancialReport(
            circleName = "Green Valley Property, LLC",
            currency = Currency.BDT,
            periodDisplay = "November 2026",
            openingBalance = 1362000L,
            totalDeposits = 100000L,
            totalExpenses = 12000L,
            netChange = 88000L,
            closingBalance = 1450000L,
            transactionCount = 5
        )

        val csv = generator.generateMonthlySummaryCsv(sampleCircle, report)

        assertThat(csv.startsWith(LedgerCsvGenerator.UTF8_BOM)).isTrue()
        assertThat(csv).contains("Month,Opening Balance,Total Deposits,Total Expenses,Net Change,Closing Balance")
        assertThat(csv).contains("November 2026,1362000,100000,12000,88000,1450000")
        // No float or poisha decimals
        assertThat(csv).doesNotContain(".00")
    }

    @Test
    fun `generateMemberStatementCsv escapes quotes and commas and counts deposits`() {
        val members = listOf(
            CircleMember(
                userId = "u-1",
                memberId = "FC-001",
                name = "Ahmed, Kawsar",
                email = "kawsar@example.com",
                role = MemberRole.ADMIN,
                status = MemberStatus.ACTIVE,
                totalContributed = 300000L,
                joinedAt = 1700000000000L,
                lastActiveAt = 1700000000000L
            ),
            CircleMember(
                userId = "u-2",
                memberId = "FC-002",
                name = "Tariqul \"Invest\" Islam",
                email = "tariqul@example.com",
                role = MemberRole.MEMBER,
                status = MemberStatus.ACTIVE,
                totalContributed = 200000L,
                joinedAt = 1700000000000L,
                lastActiveAt = 1700000000000L
            )
        )

        val deposits = listOf(
            Deposit(
                id = "dep-1",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "kawsar@example.com",
                memberName = "Ahmed, Kawsar",
                amount = 150000L,
                depositDate = 1700000000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = "Tranche 1"
            ),
            Deposit(
                id = "dep-2",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "kawsar@example.com",
                memberName = "Ahmed, Kawsar",
                amount = 150000L,
                depositDate = 1700001000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = "Tranche 2"
            ),
            Deposit(
                id = "dep-3",
                fundId = "circle-1",
                memberId = "FC-002",
                memberEmail = "tariqul@example.com",
                memberName = "Tariqul Islam",
                amount = 200000L,
                depositDate = 1700002000000L,
                paymentMethod = PaymentMethod.MOBILE_BANKING,
                note = "Contribution"
            )
        )

        val csv = generator.generateMemberStatementCsv(members, deposits)

        assertThat(csv.startsWith(LedgerCsvGenerator.UTF8_BOM)).isTrue()
        assertThat(csv).contains("Member ID,Name,Email,Total Contributed,Deposit Count,Status")
        // Escaped name with comma: "Ahmed, Kawsar"
        assertThat(csv).contains("\"Ahmed, Kawsar\",kawsar@example.com,300000,2,ACTIVE")
        // Escaped name with internal quotes: "Tariqul ""Invest"" Islam"
        assertThat(csv).contains("\"Tariqul \"\"Invest\"\" Islam\",tariqul@example.com,200000,1,ACTIVE")
    }

    @Test
    fun `generateDepositHistoryCsv handles active and reversed transactions`() {
        val deposits = listOf(
            Deposit(
                id = "dep-1",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "kawsar@example.com",
                memberName = "Kawsar Ahmed",
                amount = 50000L,
                depositDate = 1700000000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = "Payment note, with comma",
                isVoid = false
            ),
            Deposit(
                id = "dep-2",
                fundId = "circle-1",
                memberId = "FC-002",
                memberEmail = "tariqul@example.com",
                memberName = "Tariqul Islam",
                amount = 25000L,
                depositDate = 1700010000000L,
                paymentMethod = PaymentMethod.CASH,
                note = "Voided entry",
                isVoid = true,
                voidMetadata = VoidMetadata(
                    reason = "Duplicate bank voucher entry",
                    voidedAt = 1700020000000L,
                    voidedBy = "admin@example.com"
                )
            )
        )

        val csv = generator.generateDepositHistoryCsv(deposits)

        assertThat(csv.startsWith(LedgerCsvGenerator.UTF8_BOM)).isTrue()
        assertThat(csv).contains("Deposit ID,Date,Member ID,Member Name,Member Email,Amount,Payment Method,Note,Status,Void Reason")
        // Non-voided row has POSTED status and empty void reason
        assertThat(csv).contains("dep-1")
        assertThat(csv).contains("POSTED")
        assertThat(csv).contains("\"Payment note, with comma\"")

        // Voided row has REVERSED status, original whole-integer amount preserved (Invariant 4), and void reason
        assertThat(csv).contains("dep-2")
        assertThat(csv).contains("25000")
        assertThat(csv).contains("REVERSED")
        assertThat(csv).contains("Duplicate bank voucher entry")
    }

    @Test
    fun `generateExpenseHistoryCsv includes voided expenses and proper column headers`() {
        val expenses = listOf(
            Expense(
                id = "exp-1",
                fundId = "circle-1",
                amount = 12000L,
                expenseDate = 1700000000000L,
                category = ExpenseCategory.LEGAL_AND_DOCUMENTATION,
                description = "Notary fees",
                paymentMethod = PaymentMethod.CASH,
                paidBy = "Kawsar Ahmed",
                createdBy = "user-1",
                creatorEmail = "admin@example.com",
                isVoid = false
            ),
            Expense(
                id = "exp-2",
                fundId = "circle-1",
                amount = 8500L,
                expenseDate = 1700010000000L,
                category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
                description = "Office supplies",
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                paidBy = "Tariqul Islam",
                createdBy = "user-1",
                creatorEmail = "admin@example.com",
                isVoid = true,
                voidMetadata = VoidMetadata(
                    reason = "Duplicate vendor invoice",
                    voidedAt = 1700020000000L,
                    voidedBy = "admin@example.com"
                )
            )
        )

        val csv = generator.generateExpenseHistoryCsv(expenses)

        assertThat(csv.startsWith(LedgerCsvGenerator.UTF8_BOM)).isTrue()
        assertThat(csv).contains("Expense ID,Date,Category,Description,Amount,Payment Method,Paid By,Creator Email,Status,Void Reason")
        assertThat(csv).contains("exp-1")
        assertThat(csv).contains("LEGAL_AND_DOCUMENTATION")
        assertThat(csv).contains("12000")
        assertThat(csv).contains("POSTED")

        assertThat(csv).contains("exp-2")
        assertThat(csv).contains("8500")
        assertThat(csv).contains("REVERSED")
        assertThat(csv).contains("Duplicate vendor invoice")
    }

    @Test
    fun `generateDepositHistoryCsv respects custom TimeZone parameter`() {
        // Timestamp for 2026-11-20 00:00:00 UTC = 1795132800000L
        val timestampUtcMidnight = 1795132800000L
        val deposits = listOf(
            Deposit(
                id = "dep-tz",
                fundId = "circle-1",
                memberId = "FC-001",
                memberEmail = "kawsar@example.com",
                memberName = "Kawsar Ahmed",
                amount = 50000L,
                depositDate = timestampUtcMidnight,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null,
                isVoid = false
            )
        )

        val csvUtc = generator.generateDepositHistoryCsv(deposits, timeZone = java.util.TimeZone.getTimeZone("UTC"))
        assertThat(csvUtc).contains("2026-11-20")
    }

    @Test
    fun `generateMemberStatementCsv accurately counts deposits when deposit memberId matches userId`() {
        val member = CircleMember(
            userId = "auth-uid-123",
            memberId = "FC-099",
            name = "Test Member",
            email = "test@example.com",
            role = MemberRole.MEMBER,
            status = MemberStatus.ACTIVE,
            totalContributed = 100000L,
            joinedAt = 1700000000000L,
            lastActiveAt = 1700000000000L
        )

        // Deposit stored with userId as memberId (as written by RecordTransactionViewModel)
        val deposits = listOf(
            Deposit(
                id = "dep-1",
                fundId = "circle-1",
                memberId = "auth-uid-123",
                memberEmail = "test@example.com",
                memberName = "Test Member (FC-099)",
                amount = 50000L,
                depositDate = 1700000000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null,
                isVoid = false
            ),
            Deposit(
                id = "dep-2",
                fundId = "circle-1",
                memberId = "auth-uid-123",
                memberEmail = "test@example.com",
                memberName = "Test Member (FC-099)",
                amount = 50000L,
                depositDate = 1700001000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null,
                isVoid = false
            )
        )

        val csv = generator.generateMemberStatementCsv(listOf(member), deposits)
        // Verify deposit count is 2 (not 0)
        assertThat(csv).contains("FC-099,Test Member,test@example.com,100000,2,ACTIVE")
    }

    @Test
    fun `generateDepositHistoryCsv resolves raw userId to FC display identifier when members are provided`() {
        val member = CircleMember(
            userId = "auth-uid-123",
            memberId = "FC-099",
            name = "Test Member",
            email = "test@example.com",
            role = MemberRole.MEMBER,
            status = MemberStatus.ACTIVE,
            totalContributed = 100000L,
            joinedAt = 1700000000000L,
            lastActiveAt = 1700000000000L
        )

        val deposits = listOf(
            Deposit(
                id = "dep-1",
                fundId = "circle-1",
                memberId = "auth-uid-123",
                memberEmail = "test@example.com",
                memberName = "Test Member",
                amount = 50000L,
                depositDate = 1700000000000L,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                note = null,
                isVoid = false
            )
        )

        val csv = generator.generateDepositHistoryCsv(deposits, members = listOf(member))
        // Verify FC-099 appears as Member ID instead of raw auth-uid-123
        assertThat(csv).contains("dep-1")
        assertThat(csv).contains("FC-099")
    }

    @Test
    fun `escapeCsv handles special characters properly`() {
        assertThat(generator.escapeCsv("Simple Text")).isEqualTo("Simple Text")
        assertThat(generator.escapeCsv("Text, with comma")).isEqualTo("\"Text, with comma\"")
        assertThat(generator.escapeCsv("Text with \"quotes\"")).isEqualTo("\"Text with \"\"quotes\"\"\"")
        assertThat(generator.escapeCsv("Text\nwith newline")).isEqualTo("\"Text\nwith newline\"")
    }
}
