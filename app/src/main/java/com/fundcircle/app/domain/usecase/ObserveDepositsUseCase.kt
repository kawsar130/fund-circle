package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes real-time deposits subcollection for a circle.
 */
class ObserveDepositsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(fundId: String): Flow<List<Deposit>> {
        return transactionRepository.observeDeposits(fundId)
    }
}
