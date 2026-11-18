package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.repository.FundRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes real-time aggregates and metadata for a specific circle.
 */
class ObserveCircleUseCase @Inject constructor(
    private val fundRepository: FundRepository
) {
    operator fun invoke(fundId: String): Flow<FundCircle> = fundRepository.observeCircle(fundId)
}
