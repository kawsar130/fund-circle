package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe a single expense in real time.
 */
class ObserveExpenseUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(fundId: String, expenseId: String): Flow<Expense?> {
        return transactionRepository.observeExpense(fundId, expenseId)
    }
}
