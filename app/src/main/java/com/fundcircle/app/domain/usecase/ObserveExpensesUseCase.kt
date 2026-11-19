package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes real-time expenses subcollection for a circle.
 */
class ObserveExpensesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(fundId: String): Flow<List<Expense>> {
        return transactionRepository.observeExpenses(fundId)
    }
}
