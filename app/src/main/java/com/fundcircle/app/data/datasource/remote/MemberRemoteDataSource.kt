package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.CircleMemberDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source contract for observing members from /funds/{fundId}/members subcollection.
 * Invariant 3: Subcollection isolation under /funds/{fundId}/...
 */
interface MemberRemoteDataSource {
    fun observeMembers(fundId: String): Flow<List<CircleMemberDto>>
}
