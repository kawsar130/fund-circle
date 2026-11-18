package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase to persist the active circle ID for fast-path launch.
 */
class SetActiveCircleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(fundId: String): Result<Unit> {
        if (fundId.isBlank()) {
            return Result.failure(
                IllegalArgumentException("fundId must not be blank.")
            )
        }
        return authRepository.setActiveCircleId(fundId)
    }
}
