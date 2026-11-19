package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetMemberDepositsUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private lateinit var useCase: GetMemberDepositsUseCase
    private val fundId = "fund-test-101"

    @Before
    fun setUp() {
        useCase = GetMemberDepositsUseCase(transactionRepository)
    }

    @Test
    fun invoke_filtersByMemberIdAndSortsDescending() = runTest {
        val dep1 = createDeposit(id = "dep-1", memberId = "usr-1", email = "alice@example.com", date = 1000L)
        val dep2 = createDeposit(id = "dep-2", memberId = "usr-2", email = "bob@example.com", date = 2000L)
        val dep3 = createDeposit(id = "dep-3", memberId = "usr-1", email = "alice@example.com", date = 3000L)

        every { transactionRepository.observeDeposits(fundId) } returns flowOf(listOf(dep1, dep2, dep3))

        val result = useCase(fundId, "usr-1").first()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("dep-3") // newer first
        assertThat(result[1].id).isEqualTo("dep-1")
    }

    @Test
    fun invoke_filtersByEmailWhenIdDifferent() = runTest {
        val dep1 = createDeposit(id = "dep-1", memberId = "old-id", email = "alice@example.com", date = 1000L)
        val dep2 = createDeposit(id = "dep-2", memberId = "usr-2", email = "bob@example.com", date = 2000L)

        every { transactionRepository.observeDeposits(fundId) } returns flowOf(listOf(dep1, dep2))

        val result = useCase(fundId, "alice@example.com").first()

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("dep-1")
    }

    private fun createDeposit(id: String, memberId: String, email: String, date: Long) = Deposit(
        id = id,
        fundId = fundId,
        memberId = memberId,
        memberEmail = email,
        memberName = "Member Name",
        amount = 50_000L,
        depositDate = date,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        note = null,
        isVoid = false,
        voidMetadata = null
    )
}
