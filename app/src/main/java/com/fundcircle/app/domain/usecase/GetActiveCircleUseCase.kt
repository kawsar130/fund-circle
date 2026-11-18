package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase to retrieve the last active circle ID for smart launch.
 */
class GetActiveCircleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String? = authRepository.getActiveCircleId()
}
