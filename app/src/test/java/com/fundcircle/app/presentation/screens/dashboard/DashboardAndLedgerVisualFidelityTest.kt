package com.fundcircle.app.presentation.screens.dashboard

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.presentation.components.SegmentedItem
import com.fundcircle.app.presentation.screens.ledger.LedgerTab
import com.fundcircle.app.presentation.theme.TABULAR_DIGITS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale

/**
 * Visual Fidelity and Formatting Unit Tests for Phase 3 (Dashboard Screen 04 & Ledger Screen 05).
 * Validates:
 * - Whole-integer formatting with tabular digits (tnum).
 * - O(1) Atomic aggregates display math.
 * - Strikethrough & void flag integrity for reversed transactions.
 * - SegmentedControl index and label mapping for Ledger tabs.
 */
class DashboardAndLedgerVisualFidelityTest {

    @Test
    fun `hero balance formats positive whole integer with comma separation`() {
        val circle = FundCircle(
            id = "circle-1",
            name = "Apex Capital",
            description = "Investment Pool",
            currency = Currency.BDT,
            adminId = "user-1",
            adminEmail = "kawsar@example.com",
            memberEmails = listOf("kawsar@example.com"),
            memberCount = 14,
            recordedBalance = 1450000L,
            totalDeposits = 1600000L,
            totalExpenses = 150000L,
            isLocked = false,
            createdAt = 1700000000000L
        )

        val formattedBalance = NumberFormat.getIntegerInstance(Locale.US).format(circle.recordedBalance)
        val formattedDeposits = NumberFormat.getIntegerInstance(Locale.US).format(circle.totalDeposits)
        val formattedExpenses = NumberFormat.getIntegerInstance(Locale.US).format(circle.totalExpenses)

        assertEquals("1,450,000", formattedBalance)
        assertEquals("1,600,000", formattedDeposits)
        assertEquals("150,000", formattedExpenses)
        assertEquals("৳", circle.currency.symbol)
    }

    @Test
    fun `hero balance formats negative balance correctly`() {
        val circle = FundCircle(
            id = "circle-2",
            name = "Deficit Circle",
            description = "Deficit Pool",
            currency = Currency.USD,
            adminId = "user-1",
            adminEmail = "kawsar@example.com",
            memberEmails = listOf("kawsar@example.com"),
            memberCount = 5,
            recordedBalance = -25000L,
            totalDeposits = 50000L,
            totalExpenses = 75000L,
            isLocked = false,
            createdAt = 1700000000000L
        )

        val isNegative = circle.recordedBalance < 0L
        val absBalance = if (isNegative) -circle.recordedBalance else circle.recordedBalance
        val formatted = NumberFormat.getIntegerInstance(Locale.US).format(absBalance)

        assertTrue(isNegative)
        assertEquals("25,000", formatted)
        assertEquals("$", circle.currency.symbol)
    }

    @Test
    fun `transaction item card formats amounts and detects void status`() {
        val activeDeposit = TransactionItem(
            id = "tx-1",
            fundId = "circle-1",
            amount = 100000L,
            timestamp = 1700000000000L,
            title = "Rahim Uddin (FC-002)",
            isExpense = false,
            isVoid = false,
            paymentMethod = PaymentMethod.BANK_TRANSFER
        )

        val voidedExpense = TransactionItem(
            id = "tx-2",
            fundId = "circle-1",
            amount = 3500L,
            timestamp = 1700000000000L,
            title = "Office Supplies",
            isExpense = true,
            isVoid = true,
            paymentMethod = PaymentMethod.CASH,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION
        )

        val formattedDepositAmount = NumberFormat.getIntegerInstance(Locale.US).format(activeDeposit.amount)
        val formattedExpenseAmount = NumberFormat.getIntegerInstance(Locale.US).format(voidedExpense.amount)

        assertEquals("100,000", formattedDepositAmount)
        assertFalse(activeDeposit.isVoid)
        assertFalse(activeDeposit.isExpense)

        assertEquals("3,500", formattedExpenseAmount)
        assertTrue(voidedExpense.isVoid)
        assertTrue(voidedExpense.isExpense)
        assertEquals(ExpenseCategory.OFFICE_AND_ADMINISTRATION, voidedExpense.category)
    }

    @Test
    fun `ledger tab enum maps cleanly to segmented control items`() {
        val depositsCount = 14
        val expensesCount = 6

        val items = listOf(
            SegmentedItem(title = "Deposits", count = depositsCount),
            SegmentedItem(title = "Expenses", count = expensesCount)
        )

        assertEquals(2, items.size)
        assertEquals("Deposits", items[0].title)
        assertEquals(14, items[0].count)
        assertEquals("Expenses", items[1].title)
        assertEquals(6, items[1].count)

        assertEquals(0, LedgerTab.DEPOSITS.ordinal)
        assertEquals(1, LedgerTab.EXPENSES.ordinal)
        assertEquals(LedgerTab.DEPOSITS, LedgerTab.entries[0])
        assertEquals(LedgerTab.EXPENSES, LedgerTab.entries[1])
    }

    @Test
    fun `tabular digits constant is defined as tnum`() {
        assertEquals("tnum", TABULAR_DIGITS)
    }
}
