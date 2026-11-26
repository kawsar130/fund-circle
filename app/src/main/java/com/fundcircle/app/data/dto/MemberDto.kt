package com.fundcircle.app.data.dto

import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId}/members/{userId} subcollection.
 * Invariant 1: totalContributed is a 64-bit Long integer.
 * totalContributed and depositCount are maintained atomically with each deposit and reversal.
 */
@IgnoreExtraProperties
@Serializable
data class MemberDto(
    val userId: String = "",
    val memberId: String = "FC-001",
    val email: String = "",
    val name: String = "",
    val role: String = "ADMIN",
    val status: String = "ACTIVE",
    val totalContributed: Long = 0L,
    val depositCount: Long = 0L,
    val joinedAt: Long = 0L,
    val lastActiveAt: Long = 0L
)
