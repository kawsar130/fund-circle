package com.fundcircle.app.domain.model

/**
 * Pure Kotlin immutable domain representation of a collective fund circle.
 * Invariant 1: Financial totals (totalDeposits, totalExpenses, recordedBalance) are 64-bit Long integers.
 */
data class FundCircle(
    val id: String,
    val name: String,
    val description: String?,
    val currency: Currency,
    val adminId: String,
    val adminEmail: String,
    val memberEmails: List<String>,
    val memberCount: Int,
    val totalDeposits: Long,
    val totalExpenses: Long,
    val recordedBalance: Long,
    val isLocked: Boolean,
    val createdAt: Long
)
