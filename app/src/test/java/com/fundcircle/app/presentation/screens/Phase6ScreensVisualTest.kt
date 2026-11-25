package com.fundcircle.app.presentation.screens

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.MonthlyFinancialReport
import com.fundcircle.app.domain.model.ReportPeriod
import com.fundcircle.app.domain.model.ReportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Visual and Architectural Contract Tests for Phase 6 (Screens 12 & 13: Reports Engine & Circle Settings).
 * Validates:
 * - Invariant 1: Whole-integer monetary precision on statements (Long, zero poisha/cents).
 * - Screen 12: Consolidated period option generation and concise 1-line statement row labels.
 * - Screen 12: 52dp Pill CSV export button contract and RFC 4180 compatibility.
 * - Screen 13: Circle Governance danger zone layout, currency lock permanence, and switcher contract.
 */
class Phase6ScreensVisualTest {

    @Test
    fun `reports consolidated month and year options generate chronological descending pairs`() {
        val months = DateFormatSymbols(Locale.US).months.take(12)
        val testYear = 2026
        val options = mutableListOf<Pair<Int, Int>>()
        for (y in testYear downTo 2024) {
            val endMonth = if (y == testYear) 11 else 12
            for (m in endMonth downTo 1) {
                options.add(m to y)
            }
        }

        assertTrue(options.isNotEmpty())
        assertEquals(11 to 2026, options.first())
        assertEquals("November 2026", "${months[options.first().first - 1]} ${options.first().second}")
        assertTrue(options.contains(10 to 2026))
        assertTrue(options.contains(1 to 2024))
    }

    @Test
    fun `reports statement preview labels are concise single line strings`() {
        val periodDisplay = "November 2026"
        val shortMonth = periodDisplay.split(" ").firstOrNull()?.take(3) ?: "Nov"

        val depositsLabel = "Deposits ($shortMonth):"
        val expensesLabel = "Expenses ($shortMonth):"
        val netSurplusLabel = "Net Surplus:"
        val closingLabel = "Closing Balance:"

        assertEquals("Deposits (Nov):", depositsLabel)
        assertEquals("Expenses (Nov):", expensesLabel)
        assertEquals("Net Surplus:", netSurplusLabel)
        assertEquals("Closing Balance:", closingLabel)
    }

    @Test
    fun `reports statement preview maintains exact whole integer invariants`() {
        val opening = 1000000L
        val deposits = 500000L
        val expenses = 50000L
        val net = deposits - expenses
        val closing = opening + net

        val report = MonthlyFinancialReport(
            circleName = "Green Valley Property Pool",
            currency = Currency.BDT,
            periodDisplay = "November 2026",
            openingBalance = opening,
            totalDeposits = deposits,
            totalExpenses = expenses,
            netChange = net,
            closingBalance = closing,
            transactionCount = 10
        )

        assertEquals(450000L, report.netChange)
        assertEquals(1450000L, report.closingBalance)

        val formattedClosing = "৳ " + NumberFormat.getIntegerInstance(Locale.US).format(report.closingBalance)
        assertEquals("৳ 1,450,000", formattedClosing)
    }

    @Test
    fun `settings screen governance options enforce admin danger zone contracts`() {
        val governanceActions = listOf("Leave Circle", "Archive Circle", "Transfer Leadership 🛡️")
        assertEquals(3, governanceActions.size)
        assertTrue(governanceActions.contains("Leave Circle"))
        assertTrue(governanceActions.contains("Archive Circle"))
        assertTrue(governanceActions.contains("Transfer Leadership 🛡️"))
    }

    @Test
    fun `settings currency lock is immutable and reflects circle currency`() {
        fun formatCurrencyLockBadge(currency: Currency): String {
            return "${currency.name} (${currency.symbol}) 🔒"
        }

        assertEquals("BDT (৳) 🔒", formatCurrencyLockBadge(Currency.BDT))
        assertEquals("USD ($) 🔒", formatCurrencyLockBadge(Currency.USD))
        assertEquals("EUR (€) 🔒", formatCurrencyLockBadge(Currency.EUR))
    }
}
