package com.fundcircle.app.data.dto

import com.fundcircle.app.core.common.DateUtils
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Firestore DTO for /funds/{fundId}/expenses/{expenseId} subcollection documents.
 * Conforms to API Specification § 6.3.
 * Invariant 1: Amount is a 64-bit Long integer >= 1.
 * Invariant 4: isVoid marks reversed transactions without physical deletion.
 */
@IgnoreExtraProperties
@Serializable
data class ExpenseDto(
    val expenseId: String = "",
    val fundId: String = "",
    val amount: Long = 0L,
    val expenseDate: Long = 0L,
    val category: String = "",
    val description: String = "",
    val paymentMethod: String = "",
    val paidBy: String = "",
    val createdBy: String = "",
    val creatorEmail: String = "",
    @get:PropertyName("isVoid")
    @set:PropertyName("isVoid")
    var isVoid: Boolean = false,
    val voidReason: String? = null,
    val voidedAt: Long? = null,
    val voidedBy: String? = null,
    val createdAt: Long = 0L
)

fun ExpenseDto.toTransactionItem(): TransactionItem {
    val cat = ExpenseCategory.fromString(category)
    val method = PaymentMethod.fromString(paymentMethod)
    val effectiveTimestamp = if (expenseDate > 0L) expenseDate else createdAt
    val formattedDate = DateUtils.formatDisplayDate(effectiveTimestamp)
    val displayTitle = description.ifBlank { cat.displayName }
    val displaySubtitle = "$formattedDate • ${method.displayName}"

    return TransactionItem(
        id = expenseId,
        fundId = fundId,
        isExpense = true,
        title = displayTitle,
        subtitle = displaySubtitle,
        amount = amount,
        timestamp = effectiveTimestamp,
        paymentMethod = method,
        isVoid = isVoid,
        category = cat
    )
}
