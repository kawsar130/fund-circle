package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.repository.FundRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes all circles the authenticated user is currently a member or admin of.
 */
class ObserveUserCirclesUseCase @Inject constructor(
    private val fundRepository: FundRepository
) {
    operator fun invoke(): Flow<List<FundCircle>> = fundRepository.observeUserCircles()
}
