package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.WriteStatus
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
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

class RecordExpenseUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private lateinit var useCase: RecordExpenseUseCase

    private val fundId = "fund-test-123"
    private val now = 1_700_000_000_000L

    @Before
    fun setUp() {
        useCase = RecordExpenseUseCase(transactionRepository, fundRepositoryWith(fundId))
    }

    @Test
    fun invoke_rejectsZeroOrNegativeAmount() = runTest {
        val zeroExpense = Expense(
            id = "",
            fundId = fundId,
            amount = 0L,
            expenseDate = now,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "Office supplies",
            paymentMethod = PaymentMethod.CASH,
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val resultZero = useCase(fundId, zeroExpense, currentTimeMillis = now)
        assertThat(resultZero.isFailure).isTrue()
        assertThat(resultZero.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidAmount::class.java)

        val negativeExpense = zeroExpense.copy(amount = -1_000L)
        val resultNegative = useCase(fundId, negativeExpense, currentTimeMillis = now)
        assertThat(resultNegative.isFailure).isTrue()
        assertThat(resultNegative.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidAmount::class.java)

        coVerify(exactly = 0) { transactionRepository.recordExpense(any(), any()) }
    }

    @Test
    fun invoke_rejectsFutureDate() = runTest {
        val futureExpense = Expense(
            id = "",
            fundId = fundId,
            amount = 10_000L,
            expenseDate = now + 50_000L, // future
            category = ExpenseCategory.RENT_AND_MAINTENANCE,
            description = "Monthly rent",
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            paidBy = "Landlord",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = useCase(fundId, futureExpense, currentTimeMillis = now)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.FutureDateNotAllowed::class.java)

        coVerify(exactly = 0) { transactionRepository.recordExpense(any(), any()) }
    }

    @Test
    fun invoke_rejectsDescriptionTooShort() = runTest {
        val shortExpense = Expense(
            id = "",
            fundId = fundId,
            amount = 10_000L,
            expenseDate = now,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "AB", // 2 characters
            paymentMethod = PaymentMethod.CASH,
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = useCase(fundId, shortExpense, currentTimeMillis = now)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.DescriptionTooShort::class.java)

        coVerify(exactly = 0) { transactionRepository.recordExpense(any(), any()) }
    }

    @Test
    fun invoke_rejectsDescriptionTooLong() = runTest {
        val longExpense = Expense(
            id = "",
            fundId = fundId,
            amount = 10_000L,
            expenseDate = now,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "A".repeat(251), // 251 characters
            paymentMethod = PaymentMethod.CASH,
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = useCase(fundId, longExpense, currentTimeMillis = now)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.DescriptionTooLong::class.java)

        coVerify(exactly = 0) { transactionRepository.recordExpense(any(), any()) }
    }

    @Test
    fun invoke_rejectsExpense_whenCircleIsLocked() = runTest {
        useCase = RecordExpenseUseCase(transactionRepository, fundRepositoryWith(fundId, isLocked = true))
        val expense = Expense(
            id = "",
            fundId = fundId,
            amount = 15_000L,
            expenseDate = now,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "Printer toner",
            paymentMethod = PaymentMethod.CASH,
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = useCase(fundId, expense, currentTimeMillis = now)

        assertThat(result.exceptionOrNull()).isEqualTo(FundCircleError.Business.CircleLocked)
        coVerify(exactly = 0) { transactionRepository.recordExpense(any(), any()) }
    }

    @Test
    fun invoke_generatesClientSideUuidIdempotencyKey_whenBlank() = runTest {
        val expenseSlot = slot<Expense>()
        coEvery { transactionRepository.recordExpense(fundId, capture(expenseSlot)) } returns Result.success(WriteStatus.SYNCED)

        val expenseWithoutId = Expense(
            id = "",
            fundId = fundId,
            amount = 15_000L,
            expenseDate = now,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "Valid Expense Description",
            paymentMethod = PaymentMethod.CASH,
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = useCase(fundId, expenseWithoutId, currentTimeMillis = now)
        assertThat(result.isSuccess).isTrue()

        val captured = expenseSlot.captured
        assertThat(captured.id).isNotEmpty()
        UUID.fromString(captured.id)
        assertThat(captured.amount).isEqualTo(15_000L)
        assertThat(captured.description).isEqualTo("Valid Expense Description")
    }

    @Test
    fun invoke_preservesProvidedId_whenAlreadyPresent() = runTest {
        val existingId = UUID.randomUUID().toString()
        val expenseSlot = slot<Expense>()
        coEvery { transactionRepository.recordExpense(fundId, capture(expenseSlot)) } returns Result.success(WriteStatus.SYNCED)

        val expenseWithId = Expense(
            id = existingId,
            fundId = fundId,
            amount = 15_000L,
            expenseDate = now,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "Valid Expense Description",
            paymentMethod = PaymentMethod.CASH,
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = useCase(fundId, expenseWithId, currentTimeMillis = now)
        assertThat(result.isSuccess).isTrue()
        assertThat(expenseSlot.captured.id).isEqualTo(existingId)
    }
}
