package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository contract for managing collective fund circles.
 * Defined in API Specification § 5.2.
 */
interface FundRepository {
    /** Observes all circles the authenticated user belongs to (max 5) */
    fun observeUserCircles(): Flow<List<FundCircle>>

    /** Observes real-time aggregates and metadata for a specific circle */
    fun observeCircle(fundId: String): Flow<FundCircle>

    /** Creates a new circle with initial aggregate zero counters and returns the generated fundId */
    suspend fun createCircle(name: String, currency: Currency, description: String?): Result<String>

    /** Locks or unlocks circle to prevent new transactions (Admin only) */
    suspend fun toggleCircleLock(fundId: String, isLocked: Boolean): Result<Unit>
}
