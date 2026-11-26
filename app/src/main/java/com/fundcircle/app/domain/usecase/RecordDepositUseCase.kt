package com.fundcircle.app.domain.usecase

import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.WriteStatus
import com.fundcircle.app.domain.repository.FundRepository
import com.fundcircle.app.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to validate and record a new deposit.
 * Invariant 1: Validates amount >= 1L (whole positive integer).
 * Invariant 2: Executes atomic write with parent aggregate update.
 * Invariant 3: Multi-tenant subcollection isolation.
 * A locked circle accepts no new deposits.
 * Conforms to API Specification § 4.4.
 */
class RecordDepositUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val fundRepository: FundRepository
) {
    suspend operator fun invoke(
        fundId: String,
        deposit: Deposit,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Result<WriteStatus> {
        if (deposit.amount < 1L) {
            return Result.failure(FundCircleError.Validation.InvalidAmount(deposit.amount))
        }

        if (DateUtils.isDateInFuture(deposit.depositDate, currentTimeMillis)) {
            return Result.failure(FundCircleError.Validation.FutureDateNotAllowed(deposit.depositDate))
        }

        if (deposit.memberId.isBlank()) {
            return Result.failure(FundCircleError.Validation.MemberRequired)
        }

        if (fundRepository.isCircleKnownLocked(fundId)) {
            return Result.failure(FundCircleError.Business.CircleLocked)
        }

        val effectiveId = if (deposit.id.isBlank()) UUID.randomUUID().toString() else deposit.id
        val validatedDeposit = deposit.copy(id = effectiveId)

        return transactionRepository.recordDeposit(fundId, validatedDeposit)
    }
}
