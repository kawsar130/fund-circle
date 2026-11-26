package com.fundcircle.app.domain.model

/**
 * Unified ledger item representing a transaction for Dashboard recent activities and ledgers.
 * Invariant 1: Financial amounts are 64-bit Long integers >= 1. Zero Double or Float.
 * Invariant 4: isVoid marks reversed transactions without deleting them.
 */
data class TransactionItem(
    val id: String,
    val fundId: String,
    val isExpense: Boolean,
    val title: String,
    val amount: Long,
    val timestamp: Long,
    val paymentMethod: PaymentMethod,
    val isVoid: Boolean,
    val category: ExpenseCategory? = null
)
