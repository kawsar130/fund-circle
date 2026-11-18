package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.FundCircleDto
import com.fundcircle.app.data.dto.MemberDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source contract for Cloud Firestore /funds and /funds/{fundId}/members collections.
 */
interface FundRemoteDataSource {
    /** Observes real-time list of circles where user email is listed in memberEmails */
    fun observeUserCircles(userEmail: String): Flow<List<FundCircleDto>>

    /** Observes real-time aggregate state of a specific fund */
    fun observeCircle(fundId: String): Flow<FundCircleDto?>

    /** Atomically creates fund circle document and initial admin member record */
    suspend fun createCircle(fundCircle: FundCircleDto, adminMember: MemberDto): Result<Unit>

    /** Updates circle lock status */
    suspend fun toggleCircleLock(fundId: String, isLocked: Boolean): Result<Unit>
}
