package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe the most recent transactions for a circle's home dashboard.
 * Invariant 2: This use case is strictly for recent transaction feed presentation.
 * Parent totals (recordedBalance, totalDeposits, totalExpenses) are read O(1) from ObserveCircleUseCase.
 */
class ObserveRecentTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(fundId: String, limit: Int = 5): Flow<List<TransactionItem>> =
        transactionRepository.observeRecentTransactions(fundId, limit)
}
