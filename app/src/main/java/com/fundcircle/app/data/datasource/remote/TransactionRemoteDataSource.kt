package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source contract for observing transactions from Firestore subcollections.
 * Invariant 3: Subcollection isolation under /funds/{fundId}/...
 */
interface TransactionRemoteDataSource {
    fun observeRecentDeposits(fundId: String, limit: Int = 5): Flow<List<DepositDto>>
    fun observeRecentExpenses(fundId: String, limit: Int = 5): Flow<List<ExpenseDto>>
    fun observeDeposits(fundId: String): Flow<List<DepositDto>>
    fun observeExpenses(fundId: String): Flow<List<ExpenseDto>>
    suspend fun recordDeposit(fundId: String, deposit: DepositDto): Result<Unit>
    suspend fun recordExpense(fundId: String, expense: ExpenseDto): Result<Unit>
}
