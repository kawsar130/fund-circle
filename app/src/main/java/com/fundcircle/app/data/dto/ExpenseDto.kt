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

fun ExpenseDto.toDomain(): com.fundcircle.app.domain.model.Expense {
    require(amount >= 1L) { "Expense amount must be a positive integer >= 1" }
    val effectiveTimestamp = if (expenseDate > 0L) expenseDate else createdAt
    require(effectiveTimestamp > 0L) { "Expense date must be a valid epoch timestamp" }
    require(description.length in 3..250) { "Description must be between 3 and 250 characters" }

    return com.fundcircle.app.domain.model.Expense(
        id = expenseId,
        fundId = fundId,
        amount = amount,
        expenseDate = effectiveTimestamp,
        category = ExpenseCategory.fromString(category),
        description = description,
        paymentMethod = PaymentMethod.fromString(paymentMethod),
        paidBy = paidBy,
        createdBy = createdBy,
        creatorEmail = creatorEmail,
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

fun com.fundcircle.app.domain.model.Expense.toDto(): ExpenseDto = ExpenseDto(
    expenseId = id,
    fundId = fundId,
    amount = amount,
    expenseDate = expenseDate,
    category = category.displayName,
    description = description,
    paymentMethod = paymentMethod.displayName,
    paidBy = paidBy,
    createdBy = createdBy,
    creatorEmail = creatorEmail,
    isVoid = isVoid,
    voidReason = voidMetadata?.reason,
    voidedAt = voidMetadata?.voidedAt,
    voidedBy = voidMetadata?.voidedBy,
    createdAt = expenseDate
)
