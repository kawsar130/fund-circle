package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase to sign out from Firebase and clear local encrypted session.
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.signOut()
}
