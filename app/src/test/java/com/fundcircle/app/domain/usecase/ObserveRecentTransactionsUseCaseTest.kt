package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.model.TransactionItem
import com.fundcircle.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ObserveRecentTransactionsUseCaseTest {

    private val repository: TransactionRepository = mockk()
    private lateinit var useCase: ObserveRecentTransactionsUseCase

    @Before
    fun setUp() {
        useCase = ObserveRecentTransactionsUseCase(repository)
    }

    @Test
    fun invoke_delegatesDirectlyToRepositoryWithCorrectParameters() = runTest {
        val fundId = "fund-xyz"
        val limit = 5
        val expectedTransactions = listOf(
            TransactionItem(
                id = "tx-1",
                fundId = fundId,
                isExpense = false,
                title = "Alice",
                subtitle = "12 Nov 2026 • Cash",
                amount = 100_000L,
                timestamp = 1_000L,
                paymentMethod = PaymentMethod.CASH,
                isVoid = false
            )
        )

        every { repository.observeRecentTransactions(fundId, limit) } returns flowOf(expectedTransactions)

        val result = useCase(fundId, limit).first()

        assertThat(result).isEqualTo(expectedTransactions)
        verify(exactly = 1) { repository.observeRecentTransactions(fundId, limit) }
    }
}
