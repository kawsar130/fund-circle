package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe a single deposit in real time.
 */
class ObserveDepositUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(fundId: String, depositId: String): Flow<Deposit?> {
        return transactionRepository.observeDeposit(fundId, depositId)
    }
}
