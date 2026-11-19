package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.model.MemberStatus
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository contract for observing and managing circle members and KYC details.
 * Conforms to API Specification § 5.4.
 */
interface MemberRepository {
    /**
     * Observes the list of members in a circle.
     */
    fun observeMembers(fundId: String): Flow<List<CircleMember>>

    /**
     * Observes a single member profile by userId.
     */
    fun observeMember(fundId: String, userId: String): Flow<CircleMember?>

    /**
     * Retrieves strictly admin-scoped and self-scoped KYC identity PII.
     */
    suspend fun getMemberKyc(fundId: String, userId: String): Result<MemberKyc>

    /**
     * Updates KYC identity details for a member.
     */
    suspend fun updateMemberKyc(fundId: String, kyc: MemberKyc): Result<Unit>

    /**
     * Dispatches an atomic circle membership addition and in-app invitation.
     */
    suspend fun inviteMember(
        fundId: String,
        name: String,
        phone: String,
        email: String,
        memberId: String
    ): Result<Unit>

    /**
     * Updates a member's lifecycle status (e.g. ACTIVE, INACTIVE, LEFT).
     * Invariant 4: Zero physical deletes.
     */
    suspend fun toggleMemberStatus(fundId: String, userId: String, status: MemberStatus): Result<Unit>
}
