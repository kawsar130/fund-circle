package com.fundcircle.app.domain.model

/**
 * Aggregate summary for Screen 12 live on-screen statement preview.
 * Invariant 1: All financial values are 64-bit Long integers. No Double/Float.
 */
data class MonthlyFinancialReport(
    val circleName: String,
    val currency: Currency,
    val periodDisplay: String,
    val openingBalance: Long,
    val totalDeposits: Long,
    val totalExpenses: Long,
    val netChange: Long,
    val closingBalance: Long,
    val transactionCount: Int
)
