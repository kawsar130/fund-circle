package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.PaymentMethod
import com.fundcircle.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class RecordDepositUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private lateinit var useCase: RecordDepositUseCase

    private val fundId = "fund-test-123"
    private val now = 1_700_000_000_000L

    @Before
    fun setUp() {
        useCase = RecordDepositUseCase(transactionRepository)
    }

    @Test
    fun invoke_rejectsZeroOrNegativeAmount() = runTest {
        val zeroDeposit = Deposit(
            id = "",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "member@test.com",
            memberName = "Test Member",
            amount = 0L,
            depositDate = now,
            paymentMethod = PaymentMethod.CASH,
            note = null
        )

        val resultZero = useCase(fundId, zeroDeposit, currentTimeMillis = now)
        assertThat(resultZero.isFailure).isTrue()
        assertThat(resultZero.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidAmount::class.java)

        val negativeDeposit = zeroDeposit.copy(amount = -500L)
        val resultNegative = useCase(fundId, negativeDeposit, currentTimeMillis = now)
        assertThat(resultNegative.isFailure).isTrue()
        assertThat(resultNegative.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidAmount::class.java)

        coVerify(exactly = 0) { transactionRepository.recordDeposit(any(), any()) }
    }

    @Test
    fun invoke_rejectsFutureDate() = runTest {
        val futureDeposit = Deposit(
            id = "",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "member@test.com",
            memberName = "Test Member",
            amount = 50_000L,
            depositDate = now + 100_000L, // in the future
            paymentMethod = PaymentMethod.CASH,
            note = null
        )

        val result = useCase(fundId, futureDeposit, currentTimeMillis = now)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.FutureDateNotAllowed::class.java)

        coVerify(exactly = 0) { transactionRepository.recordDeposit(any(), any()) }
    }

    @Test
    fun invoke_rejectsEmptyMemberSelection() = runTest {
        val emptyMemberDeposit = Deposit(
            id = "",
            fundId = fundId,
            memberId = "   ",
            memberEmail = "member@test.com",
            memberName = "Test Member",
            amount = 50_000L,
            depositDate = now,
            paymentMethod = PaymentMethod.CASH,
            note = null
        )

        val result = useCase(fundId, emptyMemberDeposit, currentTimeMillis = now)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)

        coVerify(exactly = 0) { transactionRepository.recordDeposit(any(), any()) }
    }

    @Test
    fun invoke_generatesClientSideUuidIdempotencyKey_whenBlank() = runTest {
        val depositSlot = slot<Deposit>()
        coEvery { transactionRepository.recordDeposit(fundId, capture(depositSlot)) } returns Result.success(Unit)

        val depositWithoutId = Deposit(
            id = "",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "member@test.com",
            memberName = "Test Member",
            amount = 100_000L,
            depositDate = now,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            note = "Valid deposit"
        )

        val result = useCase(fundId, depositWithoutId, currentTimeMillis = now)
        assertThat(result.isSuccess).isTrue()

        val captured = depositSlot.captured
        assertThat(captured.id).isNotEmpty()
        // Verify valid UUID
        UUID.fromString(captured.id)
        assertThat(captured.amount).isEqualTo(100_000L)
    }

    @Test
    fun invoke_preservesProvidedId_whenAlreadyPresent() = runTest {
        val existingId = UUID.randomUUID().toString()
        val depositSlot = slot<Deposit>()
        coEvery { transactionRepository.recordDeposit(fundId, capture(depositSlot)) } returns Result.success(Unit)

        val depositWithId = Deposit(
            id = existingId,
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "member@test.com",
            memberName = "Test Member",
            amount = 100_000L,
            depositDate = now,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            note = "Valid deposit"
        )

        val result = useCase(fundId, depositWithId, currentTimeMillis = now)
        assertThat(result.isSuccess).isTrue()
        assertThat(depositSlot.captured.id).isEqualTo(existingId)
    }
}
