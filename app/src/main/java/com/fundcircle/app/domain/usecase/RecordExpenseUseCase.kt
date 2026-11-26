package com.fundcircle.app.domain.usecase

import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.WriteStatus
import com.fundcircle.app.domain.repository.FundRepository
import com.fundcircle.app.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to validate and record a new circle expense.
 * Invariant 1: Validates amount >= 1L (whole positive integer).
 * Invariant 2: Executes atomic write with parent aggregate update.
 * Invariant 3: Multi-tenant subcollection isolation.
 * A locked circle accepts no new expenses.
 * Conforms to API Specification § 4.5.
 */
class RecordExpenseUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val fundRepository: FundRepository
) {
    suspend operator fun invoke(
        fundId: String,
        expense: Expense,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Result<WriteStatus> {
        if (expense.amount < 1L) {
            return Result.failure(FundCircleError.Validation.InvalidAmount(expense.amount))
        }

        if (DateUtils.isDateInFuture(expense.expenseDate, currentTimeMillis)) {
            return Result.failure(FundCircleError.Validation.FutureDateNotAllowed(expense.expenseDate))
        }

        val trimmedDescription = expense.description.trim()
        if (trimmedDescription.length < 3) {
            return Result.failure(FundCircleError.Validation.DescriptionTooShort(trimmedDescription.length))
        }
        if (trimmedDescription.length > 250) {
            return Result.failure(FundCircleError.Validation.DescriptionTooLong(trimmedDescription.length))
        }

        if (fundRepository.isCircleKnownLocked(fundId)) {
            return Result.failure(FundCircleError.Business.CircleLocked)
        }

        val effectiveId = if (expense.id.isBlank()) UUID.randomUUID().toString() else expense.id
        val validatedExpense = expense.copy(
            id = effectiveId,
            description = trimmedDescription
        )

        return transactionRepository.recordExpense(fundId, validatedExpense)
    }
}
