package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.CircleMemberDto
import com.fundcircle.app.data.dto.MemberKycDto
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source contract for observing members and KYC from /funds/{fundId}/members subcollection.
 * Invariant 2: Atomic updates on member additions.
 * Invariant 3: Subcollection isolation under /funds/{fundId}/...
 */
interface MemberRemoteDataSource {
    fun observeMembers(fundId: String): Flow<List<CircleMemberDto>>
    fun observeMember(fundId: String, userId: String): Flow<CircleMemberDto?>
    suspend fun getMemberKyc(fundId: String, userId: String): Result<MemberKycDto>
    suspend fun updateMemberKyc(fundId: String, kyc: MemberKycDto): Result<Unit>
    suspend fun inviteMember(fundId: String, member: CircleMemberDto): Result<Unit>
    suspend fun toggleMemberStatus(fundId: String, userId: String, status: String): Result<Unit>
}
