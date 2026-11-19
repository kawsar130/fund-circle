package com.fundcircle.app.domain.model

/**
 * Immutable metadata recorded upon transaction reversal.
 * Conforms to API Specification § 6.1 and Invariant 4.
 */
data class VoidMetadata(
    val reason: String,
    val voidedAt: Long,
    val voidedBy: String
)
