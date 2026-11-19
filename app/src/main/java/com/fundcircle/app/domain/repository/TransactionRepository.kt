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
     * Executes atomic batch insertion of a deposit and updates parent circle aggregates.
     */
    suspend fun recordDeposit(fundId: String, deposit: Deposit): Result<Unit>

    /**
     * Executes atomic batch insertion of an expense and updates parent circle aggregates.
     */
    suspend fun recordExpense(fundId: String, expense: Expense): Result<Unit>
}
