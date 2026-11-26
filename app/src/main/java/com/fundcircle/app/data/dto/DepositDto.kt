package com.fundcircle.app.data.dto

import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId}/deposits/{depositId} subcollection documents.
 * Conforms to API Specification § 6.2.
 * Invariant 1: Amount is a 64-bit Long integer >= 1.
 * Invariant 4: isVoid marks reversed transactions without physical deletion.
 */
@IgnoreExtraProperties
@Serializable
data class DepositDto(
    val depositId: String = "",
    val fundId: String = "",
    val memberId: String = "",
    val memberEmail: String = "",
    val memberName: String = "",
    val amount: Long = 0L,
    val depositDate: Long = 0L,
    val paymentMethod: String = "",
    val note: String? = null,
    @get:PropertyName("isVoid")
    @set:PropertyName("isVoid")
    var isVoid: Boolean = false,
    val voidReason: String? = null,
    val voidedAt: Long? = null,
    val voidedBy: String? = null,
    val createdAt: Long = 0L
)

fun DepositDto.toTransactionItem(): TransactionItem {
    val method = PaymentMethod.fromString(paymentMethod)
    val effectiveTimestamp = if (depositDate > 0L) depositDate else createdAt
    val displayTitle = memberName.ifBlank { memberEmail }

    return TransactionItem(
        id = depositId,
        fundId = fundId,
        isExpense = false,
        title = displayTitle,
        amount = amount,
        timestamp = effectiveTimestamp,
        paymentMethod = method,
        isVoid = isVoid,
        category = null
    )
}

fun DepositDto.toDomain(): com.fundcircle.app.domain.model.Deposit {
    require(amount >= 1L) { "Deposit amount must be a positive integer >= 1" }
    val effectiveTimestamp = if (depositDate > 0L) depositDate else createdAt
    require(effectiveTimestamp > 0L) { "Deposit date must be a valid epoch timestamp" }

    return com.fundcircle.app.domain.model.Deposit(
        id = depositId,
        fundId = fundId,
        memberId = memberId,
        memberEmail = memberEmail,
        memberName = memberName,
        amount = amount,
        depositDate = effectiveTimestamp,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        note = note?.takeIf { it.isNotBlank() },
        isVoid = isVoid,
        voidMetadata = if (isVoid) {
            com.fundcircle.app.domain.model.VoidMetadata(
                reason = voidReason.orEmpty(),
                voidedAt = voidedAt ?: 0L,
                voidedBy = voidedBy.orEmpty()
            )
        } else null
    )
}

fun com.fundcircle.app.domain.model.Deposit.toDto(): DepositDto = DepositDto(
    depositId = id,
    fundId = fundId,
    memberId = memberId,
    memberEmail = memberEmail,
    memberName = memberName,
    amount = amount,
    depositDate = depositDate,
    paymentMethod = paymentMethod.wireValue,
    note = note,
    isVoid = isVoid,
    voidReason = voidMetadata?.reason,
    voidedAt = voidMetadata?.voidedAt,
    voidedBy = voidMetadata?.voidedBy,
    createdAt = depositDate
)
