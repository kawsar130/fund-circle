package com.fundcircle.app.domain.model

/**
 * Domain entity representing an operational or group expenditure incurred by a circle.
 * Invariant 1: amount is a 64-bit Long integer >= 1.
 * Invariant 4: isVoid marks reversed transactions without physical deletion.
 */
data class Expense(
    val id: String,
    val fundId: String,
    val amount: Long,
    val expenseDate: Long,
    val category: ExpenseCategory,
    val description: String,
    val paymentMethod: PaymentMethod,
    val paidBy: String,
    val createdBy: String,
    val creatorEmail: String,
    val isVoid: Boolean = false,
    val voidMetadata: VoidMetadata? = null,
    /** True while the write exists only on this device and the server has not acknowledged it (Invariant 5). */
    val isPendingSync: Boolean = false
)
