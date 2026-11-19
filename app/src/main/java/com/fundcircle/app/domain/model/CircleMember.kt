package com.fundcircle.app.domain.model

/**
 * Domain entity representing a member/stakeholder in a circle.
 * Invariant 1: totalContributed is a 64-bit Long integer >= 0.
 */
data class CircleMember(
    val userId: String,
    val memberId: String,
    val email: String,
    val name: String,
    val role: MemberRole,
    val status: MemberStatus,
    val totalContributed: Long,
    val joinedAt: Long,
    val lastActiveAt: Long
)
