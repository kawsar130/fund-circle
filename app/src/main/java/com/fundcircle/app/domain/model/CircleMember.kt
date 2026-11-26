package com.fundcircle.app.domain.model

/**
 * Domain entity representing a member/stakeholder in a circle.
 * Invariant 1: totalContributed is a 64-bit Long integer >= 0.
 * Both totals are read from the member document, never summed from the ledger (Invariant 2).
 */
data class CircleMember(
    val userId: String,
    val memberId: String,
    val email: String,
    val name: String,
    val phone: String = "",
    val role: MemberRole,
    val status: MemberStatus,
    val totalContributed: Long,
    /** Active (non-reversed) deposits recorded for this member. */
    val depositCount: Long = 0L,
    val joinedAt: Long,
    val lastActiveAt: Long
)
