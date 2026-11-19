package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.TransactionItem
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository contract for observing and managing transactions.
 * Conforms to API Specification § 5.3.
 */
interface TransactionRepository {
    /**
     * Observes the most recent transactions for the Home Dashboard.
     * Invariant 2: Aggregates are not calculated here (they are read O(1) from the parent circle).
     * Invariant 4: Reversed transactions (isVoid == true) are included.
     */
    fun observeRecentTransactions(fundId: String, limit: Int = 5): Flow<List<TransactionItem>>

    /**
     * Observes all deposits for a specific fund circle ordered descending by deposit date.
     */
    fun observeDeposits(fundId: String): Flow<List<Deposit>>

    /**
     * Observes all expenses for a specific fund circle ordered descending by expense date.
     */
    fun observeExpenses(fundId: String): Flow<List<Expense>>

    /**
     * Observes a single deposit by its ID.
     * Emits null if the deposit does not exist.
     */
    fun observeDeposit(fundId: String, depositId: String): Flow<Deposit?>

    /**
     * Observes a single expense by its ID.
     * Emits null if the expense does not exist.
     */
    fun observeExpense(fundId: String, expenseId: String): Flow<Expense?>

    /**
     * Executes atomic batch insertion of a deposit and updates parent circle aggregates.
     */
    suspend fun recordDeposit(fundId: String, deposit: Deposit): Result<Unit>

    /**
     * Executes atomic batch insertion of an expense and updates parent circle aggregates.
     */
    suspend fun recordExpense(fundId: String, expense: Expense): Result<Unit>

    /**
     * Executes atomic transactional reversal of a transaction and updates parent circle aggregates.
     * Invariant 2: Parent totals updated atomically via runTransaction.
     * Invariant 4: Marks isVoid = true with mandatory audit metadata; zero hard deletes.
     */
    suspend fun voidTransaction(
        fundId: String,
        isExpense: Boolean,
        transactionId: String,
        originalAmount: Long,
        voidReason: String,
        adminEmail: String
    ): Result<Unit>
}
