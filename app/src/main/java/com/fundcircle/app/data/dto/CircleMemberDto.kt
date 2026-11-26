package com.fundcircle.app.data.dto

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId}/members/{userId} subcollection.
 * Invariant 1: totalContributed is a 64-bit Long integer >= 0.
 * totalContributed and depositCount are maintained atomically with each deposit and reversal.
 */
@IgnoreExtraProperties
@Serializable
data class CircleMemberDto(
    val userId: String = "",
    val memberId: String = "FC-001",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "MEMBER",
    val status: String = "ACTIVE",
    val totalContributed: Long = 0L,
    val depositCount: Long = 0L,
    val joinedAt: Long = 0L,
    val lastActiveAt: Long = 0L
)

fun CircleMemberDto.toDomain(): CircleMember = CircleMember(
    userId = userId,
    memberId = memberId,
    email = email,
    name = name,
    phone = phone,
    role = MemberRole.fromString(role),
    status = MemberStatus.fromString(status),
    totalContributed = totalContributed,
    depositCount = depositCount,
    joinedAt = joinedAt,
    lastActiveAt = lastActiveAt
)

fun CircleMember.toDto(): CircleMemberDto = CircleMemberDto(
    userId = userId,
    memberId = memberId,
    email = email,
    name = name,
    phone = phone,
    role = role.name,
    status = status.name,
    totalContributed = totalContributed,
    depositCount = depositCount,
    joinedAt = joinedAt,
    lastActiveAt = lastActiveAt
)
