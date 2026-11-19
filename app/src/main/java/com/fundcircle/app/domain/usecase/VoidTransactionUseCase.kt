package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Use case to validate and execute the immutable transaction reversal protocol.
 *
 * Invariant 1: Validates originalAmount is a whole positive integer >= 1.
 * Invariant 2: Transactional atomic balance impact.
 * Invariant 4: Mandatory reason (5-250 chars) and admin attribution with zero hard deletes.
 */
class VoidTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        fundId: String,
        isExpense: Boolean,
        transactionId: String,
        originalAmount: Long,
        voidReason: String,
        adminEmail: String
    ): Result<Unit> {
        val trimmedReason = voidReason.trim()
        if (trimmedReason.length !in 5..250) {
            return Result.failure(
                FundCircleError.Validation.VoidReasonRequired(trimmedReason.length)
            )
        }

        if (originalAmount < 1L) {
            return Result.failure(
                FundCircleError.Validation.InvalidAmount(originalAmount)
            )
        }

        if (adminEmail.isBlank()) {
            return Result.failure(
                FundCircleError.Authorization.AdminPrivilegeRequired
            )
        }

        if (fundId.isBlank() || transactionId.isBlank()) {
            return Result.failure(
                FundCircleError.NotFound.ResourceNotFound
            )
        }

        return transactionRepository.voidTransaction(
            fundId = fundId,
            isExpense = isExpense,
            transactionId = transactionId,
            originalAmount = originalAmount,
            voidReason = trimmedReason,
            adminEmail = adminEmail.trim()
        )
    }
}
