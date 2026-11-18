package com.fundcircle.app.domain.model

/**
 * Access control roles within a circle workspace.
 */
enum class MemberRole {
    ADMIN,
    MEMBER;

    companion object {
        fun fromString(value: String): MemberRole = when (value.trim().uppercase()) {
            "ADMIN" -> ADMIN
            "MEMBER" -> MEMBER
            else -> MEMBER
        }
    }
}
