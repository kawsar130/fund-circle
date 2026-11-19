package com.fundcircle.app.data.repository

import com.fundcircle.app.data.datasource.remote.TransactionRemoteDataSource
import com.fundcircle.app.data.dto.DepositDto
import com.fundcircle.app.data.dto.ExpenseDto
import com.fundcircle.app.domain.model.Deposit
import com.fundcircle.app.domain.model.Expense
import com.fundcircle.app.domain.model.ExpenseCategory
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.PaymentMethod
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TransactionRepositoryImplTest {

    private val remoteDataSource: TransactionRemoteDataSource = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: TransactionRepositoryImpl

    private val fundId = "fund-test-123"

    @Before
    fun setUp() {
        repository = TransactionRepositoryImpl(
            transactionRemoteDataSource = remoteDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun observeRecentTransactions_combinesDepositsAndExpensesChronologicallyDescending() = runTest(testDispatcher) {
        val deposit1 = DepositDto(
            depositId = "dep-1",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "alice@example.com",
            memberName = "Alice",
            amount = 100_000L,
            depositDate = 1_000L,
            paymentMethod = "Cash",
            isVoid = false
        )
        val deposit2 = DepositDto(
            depositId = "dep-2",
            fundId = fundId,
            memberId = "mem-2",
            memberEmail = "bob@example.com",
            memberName = "Bob",
            amount = 200_000L,
            depositDate = 3_000L,
            paymentMethod = "Bank Transfer",
            isVoid = false
        )

        val expense1 = ExpenseDto(
            expenseId = "exp-1",
            fundId = fundId,
            amount = 50_000L,
            expenseDate = 2_000L,
            category = "Office & Administration",
            description = "Stationery",
            paymentMethod = "Mobile Banking",
            isVoid = false
        )
        val expense2 = ExpenseDto(
            expenseId = "exp-2",
            fundId = fundId,
            amount = 30_000L,
            expenseDate = 4_000L,
            category = "Others",
            description = "Supplies",
            paymentMethod = "Cash",
            isVoid = false
        )

        every { remoteDataSource.observeRecentDeposits(fundId, 5) } returns flowOf(listOf(deposit2, deposit1))
        every { remoteDataSource.observeRecentExpenses(fundId, 5) } returns flowOf(listOf(expense2, expense1))

        val result = repository.observeRecentTransactions(fundId, 5).first()

        assertThat(result).hasSize(4)
        // Ordering should be: exp-2 (4000) -> dep-2 (3000) -> exp-1 (2000) -> dep-1 (1000)
        assertThat(result[0].id).isEqualTo("exp-2")
        assertThat(result[0].timestamp).isEqualTo(4_000L)
        assertThat(result[0].isExpense).isTrue()
        assertThat(result[0].amount).isEqualTo(30_000L)
        assertThat(result[0].paymentMethod).isEqualTo(PaymentMethod.CASH)
        assertThat(result[0].category).isEqualTo(ExpenseCategory.OTHERS)

        assertThat(result[1].id).isEqualTo("dep-2")
        assertThat(result[1].timestamp).isEqualTo(3_000L)
        assertThat(result[1].isExpense).isFalse()
        assertThat(result[1].amount).isEqualTo(200_000L)
        assertThat(result[1].paymentMethod).isEqualTo(PaymentMethod.BANK_TRANSFER)

        assertThat(result[2].id).isEqualTo("exp-1")
        assertThat(result[2].timestamp).isEqualTo(2_000L)
        assertThat(result[2].isExpense).isTrue()
        assertThat(result[2].category).isEqualTo(ExpenseCategory.OFFICE_AND_ADMINISTRATION)

        assertThat(result[3].id).isEqualTo("dep-1")
        assertThat(result[3].timestamp).isEqualTo(1_000L)
        assertThat(result[3].isExpense).isFalse()
    }

    @Test
    fun observeRecentTransactions_respectsLimit() = runTest(testDispatcher) {
        val deposits = (1..4).map { i ->
            DepositDto(
                depositId = "dep-$i",
                fundId = fundId,
                amount = 10_000L * i,
                depositDate = 1_000L * i,
                paymentMethod = "Cash"
            )
        }
        val expenses = (1..4).map { i ->
            ExpenseDto(
                expenseId = "exp-$i",
                fundId = fundId,
                amount = 5_000L * i,
                expenseDate = 1_000L * i + 500L,
                category = "Others",
                description = "Expense $i",
                paymentMethod = "Cash"
            )
        }

        every { remoteDataSource.observeRecentDeposits(fundId, 5) } returns flowOf(deposits)
        every { remoteDataSource.observeRecentExpenses(fundId, 5) } returns flowOf(expenses)

        val result = repository.observeRecentTransactions(fundId, 5).first()

        assertThat(result).hasSize(5)
        // 8 total transactions, top 5 timestamps: exp-4 (4500), dep-4 (4000), exp-3 (3500), dep-3 (3000), exp-2 (2500)
        assertThat(result.map { it.id }).containsExactly("exp-4", "dep-4", "exp-3", "dep-3", "exp-2").inOrder()
    }

    @Test
    fun observeRecentTransactions_handlesEmptySubcollectionsGracefully() = runTest(testDispatcher) {
        every { remoteDataSource.observeRecentDeposits(fundId, 5) } returns flowOf(emptyList())
        every { remoteDataSource.observeRecentExpenses(fundId, 5) } returns flowOf(emptyList())

        val result = repository.observeRecentTransactions(fundId, 5).first()

        assertThat(result).isEmpty()
    }

    @Test
    fun observeRecentTransactions_breaksTiesByDescendingIdWhenTimestampsAreEqual() = runTest(testDispatcher) {
        val sameTimestamp = 2_000L
        val depositA = DepositDto(
            depositId = "dep-a",
            fundId = fundId,
            amount = 10_000L,
            depositDate = sameTimestamp,
            paymentMethod = "Cash"
        )
        val depositZ = DepositDto(
            depositId = "dep-z",
            fundId = fundId,
            amount = 20_000L,
            depositDate = sameTimestamp,
            paymentMethod = "Cash"
        )

        every { remoteDataSource.observeRecentDeposits(fundId, 5) } returns flowOf(listOf(depositA, depositZ))
        every { remoteDataSource.observeRecentExpenses(fundId, 5) } returns flowOf(emptyList())

        val result = repository.observeRecentTransactions(fundId, 5).first()

        assertThat(result.map { it.id }).containsExactly("dep-z", "dep-a").inOrder()
    }

    @Test
    fun observeRecentTransactions_retainsReversedTransactionsWithOriginalAmount() = runTest(testDispatcher) {
        val voidedDeposit = DepositDto(
            depositId = "dep-voided",
            fundId = fundId,
            memberName = "Sajjad",
            amount = 50_000L,
            depositDate = 5_000L,
            paymentMethod = "Cash",
            isVoid = true,
            voidReason = "Entered wrong amount",
            voidedAt = 5_500L,
            voidedBy = "admin@example.com"
        )

        every { remoteDataSource.observeRecentDeposits(fundId, 5) } returns flowOf(listOf(voidedDeposit))
        every { remoteDataSource.observeRecentExpenses(fundId, 5) } returns flowOf(emptyList())

        val result = repository.observeRecentTransactions(fundId, 5).first()

        assertThat(result).hasSize(1)
        val item = result[0]
        assertThat(item.id).isEqualTo("dep-voided")
        // Invariant 4: isVoid is true, amount is preserved as original positive integer
        assertThat(item.isVoid).isTrue()
        assertThat(item.amount).isEqualTo(50_000L)
    }

    @Test
    fun observeRecentTransactions_whenFirestoreError_mapsToDomainError() = runTest(testDispatcher) {
        val exception = FirebaseFirestoreException(
            "Access denied",
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )

        every { remoteDataSource.observeRecentDeposits(fundId, 5) } returns flow { throw exception }
        every { remoteDataSource.observeRecentExpenses(fundId, 5) } returns flowOf(emptyList())

        try {
            repository.observeRecentTransactions(fundId, 5).first()
            assert(false) { "Expected FundCircleError.Authorization.NotAuthorized" }
        } catch (e: Throwable) {
            assertThat(e).isInstanceOf(FundCircleError.Authorization.NotAuthorized::class.java)
        }
    }

    @Test
    fun observeDeposits_mapsDtosToDomainEntitiesDescending() = runTest(testDispatcher) {
        val dto1 = DepositDto(
            depositId = "dep-1",
            fundId = fundId,
            memberId = "mem-1",
            memberName = "Rahim",
            amount = 100_000L,
            depositDate = 2_000L,
            paymentMethod = "Bank Transfer"
        )
        val dto2 = DepositDto(
            depositId = "dep-2",
            fundId = fundId,
            memberId = "mem-2",
            memberName = "Karim",
            amount = 50_000L,
            depositDate = 1_000L,
            paymentMethod = "Cash"
        )

        every { remoteDataSource.observeDeposits(fundId) } returns flowOf(listOf(dto1, dto2))

        val result = repository.observeDeposits(fundId).first()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("dep-1")
        assertThat(result[0].amount).isEqualTo(100_000L)
        assertThat(result[1].id).isEqualTo("dep-2")
        assertThat(result[1].amount).isEqualTo(50_000L)
    }

    @Test
    fun observeDeposits_ignoresCorruptedDtosResiliently() = runTest(testDispatcher) {
        val validDto = DepositDto(
            depositId = "dep-valid",
            fundId = fundId,
            memberId = "mem-1",
            memberName = "Rahim",
            amount = 100_000L,
            depositDate = 2_000L,
            paymentMethod = "Cash"
        )
        val corruptedDto = DepositDto(
            depositId = "dep-invalid",
            fundId = fundId,
            memberId = "mem-2",
            memberName = "Corrupt",
            amount = -500L,
            depositDate = 1_000L,
            paymentMethod = "Cash"
        )

        every { remoteDataSource.observeDeposits(fundId) } returns flowOf(listOf(validDto, corruptedDto))

        val result = repository.observeDeposits(fundId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("dep-valid")
    }

    @Test
    fun observeExpenses_mapsDtosToDomainEntitiesDescending() = runTest(testDispatcher) {
        val dto1 = ExpenseDto(
            expenseId = "exp-1",
            fundId = fundId,
            amount = 20_000L,
            expenseDate = 2_000L,
            category = "Office & Administration",
            description = "Desk",
            paymentMethod = "Cash"
        )

        every { remoteDataSource.observeExpenses(fundId) } returns flowOf(listOf(dto1))

        val result = repository.observeExpenses(fundId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo("exp-1")
        assertThat(result[0].category).isEqualTo(ExpenseCategory.OFFICE_AND_ADMINISTRATION)
        assertThat(result[0].amount).isEqualTo(20_000L)
    }

    @Test
    fun recordDeposit_callsRemoteDataSource_andReturnsSuccess() = runTest(testDispatcher) {
        val depositSlot = slot<DepositDto>()
        coEvery { remoteDataSource.recordDeposit(fundId, capture(depositSlot)) } returns Result.success(Unit)

        val deposit = Deposit(
            id = "uuid-dep-1",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "member@test.com",
            memberName = "Test Member",
            amount = 75_000L,
            depositDate = 1_000L,
            paymentMethod = PaymentMethod.CASH,
            note = "Tranche 1"
        )

        val result = repository.recordDeposit(fundId, deposit)

        assertThat(result.isSuccess).isTrue()
        val captured = depositSlot.captured
        assertThat(captured.depositId).isEqualTo("uuid-dep-1")
        assertThat(captured.amount).isEqualTo(75_000L)
        assertThat(captured.fundId).isEqualTo(fundId)
    }

    @Test
    fun recordExpense_callsRemoteDataSource_andReturnsSuccess() = runTest(testDispatcher) {
        val expenseSlot = slot<ExpenseDto>()
        coEvery { remoteDataSource.recordExpense(fundId, capture(expenseSlot)) } returns Result.success(Unit)

        val expense = Expense(
            id = "uuid-exp-1",
            fundId = fundId,
            amount = 15_000L,
            expenseDate = 1_000L,
            category = ExpenseCategory.OFFICE_AND_ADMINISTRATION,
            description = "Office Chair",
            paymentMethod = PaymentMethod.MOBILE_BANKING,
            paidBy = "Admin",
            createdBy = "admin-uid",
            creatorEmail = "admin@test.com"
        )

        val result = repository.recordExpense(fundId, expense)

        assertThat(result.isSuccess).isTrue()
        val captured = expenseSlot.captured
        assertThat(captured.expenseId).isEqualTo("uuid-exp-1")
        assertThat(captured.amount).isEqualTo(15_000L)
        assertThat(captured.description).isEqualTo("Office Chair")
    }

    @Test
    fun recordDeposit_whenError_mapsToDomainError() = runTest(testDispatcher) {
        val exception = FirebaseFirestoreException(
            "Permission denied",
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )
        coEvery { remoteDataSource.recordDeposit(any(), any()) } returns Result.failure(exception)

        val deposit = Deposit(
            id = "dep-err",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "m@test.com",
            memberName = "M",
            amount = 10_000L,
            depositDate = 1_000L,
            paymentMethod = PaymentMethod.CASH,
            note = null
        )

        val result = repository.recordDeposit(fundId, deposit)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Authorization.NotAuthorized::class.java)
    }

    @Test
    fun observeDeposit_whenFound_mapsDtoToDomainEntity() = runTest(testDispatcher) {
        val dto = DepositDto(
            depositId = "dep-101",
            fundId = fundId,
            memberId = "mem-1",
            memberEmail = "rahim@example.com",
            memberName = "Rahim Uddin",
            amount = 100_000L,
            depositDate = 2_000L,
            paymentMethod = "Bank Transfer",
            note = "Contribution 1",
            isVoid = false
        )
        every { remoteDataSource.observeDeposit(fundId, "dep-101") } returns flowOf(dto)

        val result = repository.observeDeposit(fundId, "dep-101").first()

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("dep-101")
        assertThat(result?.amount).isEqualTo(100_000L)
        assertThat(result?.paymentMethod).isEqualTo(PaymentMethod.BANK_TRANSFER)
        assertThat(result?.memberName).isEqualTo("Rahim Uddin")
        assertThat(result?.isVoid).isFalse()
    }

    @Test
    fun observeDeposit_whenNotFound_emitsNull() = runTest(testDispatcher) {
        every { remoteDataSource.observeDeposit(fundId, "dep-missing") } returns flowOf(null)

        val result = repository.observeDeposit(fundId, "dep-missing").first()

        assertThat(result).isNull()
    }

    @Test
    fun observeExpense_whenFound_mapsDtoToDomainEntity() = runTest(testDispatcher) {
        val dto = ExpenseDto(
            expenseId = "exp-202",
            fundId = fundId,
            amount = 15_000L,
            expenseDate = 3_000L,
            category = "Office & Administration",
            description = "Desk Supplies",
            paymentMethod = "Cash",
            paidBy = "Vendor",
            createdBy = "admin-uid",
            creatorEmail = "admin@example.com",
            isVoid = false
        )
        every { remoteDataSource.observeExpense(fundId, "exp-202") } returns flowOf(dto)

        val result = repository.observeExpense(fundId, "exp-202").first()

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("exp-202")
        assertThat(result?.amount).isEqualTo(15_000L)
        assertThat(result?.category).isEqualTo(ExpenseCategory.OFFICE_AND_ADMINISTRATION)
        assertThat(result?.description).isEqualTo("Desk Supplies")
    }

    @Test
    fun observeExpense_whenNotFound_emitsNull() = runTest(testDispatcher) {
        every { remoteDataSource.observeExpense(fundId, "exp-missing") } returns flowOf(null)

        val result = repository.observeExpense(fundId, "exp-missing").first()

        assertThat(result).isNull()
    }

    @Test
    fun voidTransaction_forDeposit_delegatesToRemoteDataSourceSuccessfully() = runTest(testDispatcher) {
        coEvery {
            remoteDataSource.voidTransaction(
                fundId = fundId,
                isExpense = false,
                transactionId = "dep-101",
                originalAmount = 100_000L,
                voidReason = "Accidental duplicate entry",
                adminEmail = "admin@example.com"
            )
        } returns Result.success(Unit)

        val result = repository.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-101",
            originalAmount = 100_000L,
            voidReason = "Accidental duplicate entry",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isSuccess).isTrue()
        coVerify {
            remoteDataSource.voidTransaction(
                fundId = fundId,
                isExpense = false,
                transactionId = "dep-101",
                originalAmount = 100_000L,
                voidReason = "Accidental duplicate entry",
                adminEmail = "admin@example.com"
            )
        }
    }

    @Test
    fun voidTransaction_forExpense_delegatesToRemoteDataSourceSuccessfully() = runTest(testDispatcher) {
        coEvery {
            remoteDataSource.voidTransaction(
                fundId = fundId,
                isExpense = true,
                transactionId = "exp-202",
                originalAmount = 25_000L,
                voidReason = "Incorrect invoice billed",
                adminEmail = "admin@example.com"
            )
        } returns Result.success(Unit)

        val result = repository.voidTransaction(
            fundId = fundId,
            isExpense = true,
            transactionId = "exp-202",
            originalAmount = 25_000L,
            voidReason = "Incorrect invoice billed",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isSuccess).isTrue()
        coVerify {
            remoteDataSource.voidTransaction(
                fundId = fundId,
                isExpense = true,
                transactionId = "exp-202",
                originalAmount = 25_000L,
                voidReason = "Incorrect invoice billed",
                adminEmail = "admin@example.com"
            )
        }
    }

    @Test
    fun voidTransaction_whenAlreadyVoided_mapsToTransactionAlreadyVoidedConflict() = runTest(testDispatcher) {
        coEvery {
            remoteDataSource.voidTransaction(any(), any(), any(), any(), any(), any())
        } returns Result.failure(IllegalStateException("Transaction is already reversed."))

        val result = repository.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-101",
            originalAmount = 100_000L,
            voidReason = "Accidental duplicate entry",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Conflict.TransactionAlreadyVoided::class.java)
        val conflict = result.exceptionOrNull() as FundCircleError.Conflict.TransactionAlreadyVoided
        assertThat(conflict.txId).isEqualTo("dep-101")
    }

    @Test
    fun voidTransaction_whenNotFound_mapsToResourceNotFound() = runTest(testDispatcher) {
        coEvery {
            remoteDataSource.voidTransaction(any(), any(), any(), any(), any(), any())
        } returns Result.failure(NoSuchElementException("Transaction not found"))

        val result = repository.voidTransaction(
            fundId = fundId,
            isExpense = false,
            transactionId = "dep-missing",
            originalAmount = 100_000L,
            voidReason = "Accidental duplicate entry",
            adminEmail = "admin@example.com"
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.NotFound.ResourceNotFound::class.java)
    }
}
