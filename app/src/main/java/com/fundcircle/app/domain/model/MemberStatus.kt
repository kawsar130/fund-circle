package com.fundcircle.app.domain.model

/**
 * Lifecycle status of a member within a circle workspace.
 */
enum class MemberStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    LEFT;

    companion object {
        fun fromString(value: String): MemberStatus = entries.firstOrNull {
            it.name.equals(value.trim(), ignoreCase = true)
        } ?: PENDING
    }
}
