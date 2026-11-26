package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.repository.FundRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

internal fun testCircle(fundId: String, isLocked: Boolean = false) = FundCircle(
    id = fundId,
    name = "Test Circle",
    description = null,
    currency = Currency.BDT,
    adminId = "admin-uid",
    adminEmail = "admin@example.com",
    memberEmails = listOf("admin@example.com"),
    memberCount = 1,
    totalDeposits = 0L,
    totalExpenses = 0L,
    recordedBalance = 0L,
    isLocked = isLocked,
    createdAt = 1L
)

/** A FundRepository whose circle is unlocked unless [isLocked] is set. */
internal fun fundRepositoryWith(fundId: String, isLocked: Boolean = false): FundRepository =
    mockk<FundRepository>(relaxed = true).also {
        every { it.observeCircle(fundId) } returns flowOf(testCircle(fundId, isLocked))
    }
