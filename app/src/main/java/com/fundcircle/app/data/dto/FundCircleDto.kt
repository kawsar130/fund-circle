package com.fundcircle.app.data.dto

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId} root document.
 * Invariant 1: Financial fields are 64-bit Long integers.
 */
@IgnoreExtraProperties
@Serializable
data class FundCircleDto(
    val fundId: String = "",
    val name: String = "",
    val description: String? = null,
    val currency: String = "BDT",
    val adminId: String = "",
    val adminEmail: String = "",
    val memberEmails: List<String> = emptyList(),
    val memberCount: Int = 1,
    val totalDeposits: Long = 0L,
    val totalExpenses: Long = 0L,
    val recordedBalance: Long = 0L,
    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

fun FundCircleDto.toDomain(): FundCircle = FundCircle(
    id = fundId,
    name = name,
    description = description,
    currency = Currency.fromStringOrNull(currency) ?: Currency.BDT,
    adminId = adminId,
    adminEmail = adminEmail,
    memberEmails = memberEmails,
    memberCount = memberCount,
    totalDeposits = totalDeposits,
    totalExpenses = totalExpenses,
    recordedBalance = recordedBalance,
    isLocked = isLocked,
    createdAt = createdAt
)
