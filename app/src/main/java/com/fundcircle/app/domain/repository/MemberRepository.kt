package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.CircleMember
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository contract for observing circle members.
 * Conforms to API Specification § 5.4.
 */
interface MemberRepository {
    /**
     * Observes the list of members in a circle.
     */
    fun observeMembers(fundId: String): Flow<List<CircleMember>>
}
