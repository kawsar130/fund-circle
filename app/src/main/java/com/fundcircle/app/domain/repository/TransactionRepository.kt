package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.TransactionItem
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository contract for observing and managing transactions.
 * Conforms to API Specification § 5.3.
 */
interface TransactionRepository {
    /**
     * Observes the most recent transactions for the Home Dashboard.
     * Invariant 2: Aggregates are not calculated here (they are read O(1) from the parent circle).
     * Invariant 4: Reversed transactions (isVoid == true) are included.
     */
    fun observeRecentTransactions(fundId: String, limit: Int = 5): Flow<List<TransactionItem>>
}
