package com.fundcircle.app.data.dto

import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId}/deposits/{depositId} subcollection documents.
 * Conforms to API Specification § 6.2.
 * Invariant 1: Amount is a 64-bit Long integer >= 1.
 * Invariant 4: isVoid marks reversed transactions without physical deletion.
 */
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
    val formattedDate = DateUtils.formatDisplayDate(effectiveTimestamp)
    val displayTitle = memberName.ifBlank { memberEmail }
    val displaySubtitle = "$formattedDate • ${method.displayName}"

    return TransactionItem(
        id = depositId,
        fundId = fundId,
        isExpense = false,
        title = displayTitle,
        subtitle = displaySubtitle,
        amount = amount,
        timestamp = effectiveTimestamp,
        paymentMethod = method,
        isVoid = isVoid,
        category = null
    )
}
