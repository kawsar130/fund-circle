package com.fundcircle.app.domain.model

/**
 * Domain entity representing strictly admin-scoped and self-scoped KYC PII.
 * Conforms to API Specification § 4.3 & § 6.6.
 * Invariant 3: Multi-tenant physical isolation under /funds/{fundId}/members/{userId}/kyc/identity.
 */
data class MemberKyc(
    val userId: String,
    val nidNumber: String,
    val maskedNid: String,
    val nomineeName: String,
    val nomineePhone: String,
    val nomineeRelation: String,
    val verifiedByAdmin: Boolean = false,
    val updatedAt: Long = 0L
)
