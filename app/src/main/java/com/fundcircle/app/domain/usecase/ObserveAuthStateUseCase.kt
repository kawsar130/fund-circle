package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to observe real-time authentication session state.
 */
class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<UserSession?> = authRepository.userSession
}
