package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * UseCase to authenticate a user using a Google ID token from Credential Manager.
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<UserSession> {
        if (idToken.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Google ID token must not be blank.")
            )
        }
        return authRepository.signInWithGoogle(idToken)
    }
}
