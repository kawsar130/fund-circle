package com.fundcircle.app.domain.model

/**
 * Domain entity representing an individual capital contribution to a circle.
 * Invariant 1: amount is a 64-bit Long integer >= 1.
 * Invariant 4: isVoid marks reversed transactions without physical deletion.
 */
data class Deposit(
    val id: String,
    val fundId: String,
    val memberId: String,
    val memberEmail: String,
    val memberName: String,
    val amount: Long,
    val depositDate: Long,
    val paymentMethod: PaymentMethod,
    val note: String?,
    val isVoid: Boolean = false,
    val voidMetadata: VoidMetadata? = null,
    /** True while the write exists only on this device and the server has not acknowledged it (Invariant 5). */
    val isPendingSync: Boolean = false
)
