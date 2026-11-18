package com.fundcircle.app.domain.model

/**
 * User authentication session model as specified in API Specification § 6.7.
 */
data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val idToken: String,
    val activeCircleId: String? = null
)
