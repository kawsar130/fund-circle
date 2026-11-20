package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.repository.FundRepository
import javax.inject.Inject

/**
 * UseCase to transfer circle admin ownership to an active stakeholder.
 * Updates circle adminId/adminEmail and updates member roles in Firestore batch.
 */
class TransferAdminOwnershipUseCase @Inject constructor(
    private val fundRepository: FundRepository
) {

    suspend operator fun invoke(
        fundId: String,
        currentAdminUserId: String,
        newAdminUserId: String,
        newAdminEmail: String,
        circle: FundCircle
    ): Result<Unit> {
        if (currentAdminUserId.isBlank() || circle.adminId != currentAdminUserId) {
            return Result.failure(IllegalStateException("Only active Admin can transfer circle ownership."))
        }
        if (newAdminUserId.isBlank()) {
            return Result.failure(IllegalArgumentException("Target member must be selected."))
        }
        if (newAdminUserId == currentAdminUserId) {
            return Result.failure(IllegalArgumentException("Cannot transfer admin ownership to yourself."))
        }

        return fundRepository.transferAdminRole(
            fundId = fundId,
            currentAdminUserId = currentAdminUserId,
            newAdminUserId = newAdminUserId,
            newAdminEmail = newAdminEmail
        )
    }
}
