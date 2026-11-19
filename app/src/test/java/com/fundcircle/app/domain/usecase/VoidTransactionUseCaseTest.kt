package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.TransactionRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class VoidTransactionUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private lateinit var useCase: VoidTransactionUseCase

    private val fundId = "fund-circle-1"
    private val txId = "tx-001"
    private val adminEmail = "admin@example.com"
    private val validReason = "Duplicate bank payment voucher entry."

    @Before
    fun setUp() {
        useCase = VoidTransactionUseCase(transactionRepository)
    }

    @Test
    fun invoke_whenReasonUnder5Chars_returnsVoidReasonRequiredError() = runTest {
        val shortReason = "No"

        val result = useCase(
            fundId = fundId,
            isExpense = false,
            transactionId = txId,
            originalAmount = 50_000L,
            voidReason = shortReason,
            adminEmail = adminEmail
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.VoidReasonRequired::class.java)
        coVerify(exactly = 0) { transactionRepository.voidTransaction(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_whenReasonOnlyWhitespaceUnder5Chars_returnsVoidReasonRequiredError() = runTest {
        val whitespaceReason = "  abc  " // trimmed length is 3

        val result = useCase(
            fundId = fundId,
            isExpense = false,
            transactionId = txId,
            originalAmount = 50_000L,
            voidReason = whitespaceReason,
            adminEmail = adminEmail
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.VoidReasonRequired::class.java)
    }

    @Test
    fun invoke_whenReasonExceeds250Chars_returnsVoidReasonRequiredError() = runTest {
        val overLongReason = "A".repeat(251)

        val result = useCase(
            fundId = fundId,
            isExpense = false,
            transactionId = txId,
            originalAmount = 50_000L,
            voidReason = overLongReason,
            adminEmail = adminEmail
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.VoidReasonRequired::class.java)
        coVerify(exactly = 0) { transactionRepository.voidTransaction(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_whenAmountLessThan1_returnsInvalidAmountError() = runTest {
        val result = useCase(
            fundId = fundId,
            isExpense = false,
            transactionId = txId,
            originalAmount = 0L,
            voidReason = validReason,
            adminEmail = adminEmail
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidAmount::class.java)
        coVerify(exactly = 0) { transactionRepository.voidTransaction(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_whenAdminEmailBlank_returnsAdminPrivilegeRequiredError() = runTest {
        val result = useCase(
            fundId = fundId,
            isExpense = false,
            transactionId = txId,
            originalAmount = 50_000L,
            voidReason = validReason,
            adminEmail = "   "
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Authorization.AdminPrivilegeRequired::class.java)
        coVerify(exactly = 0) { transactionRepository.voidTransaction(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun invoke_whenFundIdOrTxIdBlank_returnsResourceNotFoundError() = runTest {
        val resultBlankFund = useCase(
            fundId = "",
            isExpense = false,
            transactionId = txId,
            originalAmount = 50_000L,
            voidReason = validReason,
            adminEmail = adminEmail
        )
        assertThat(resultBlankFund.isFailure).isTrue()
        assertThat(resultBlankFund.exceptionOrNull()).isInstanceOf(FundCircleError.NotFound.ResourceNotFound::class.java)

        val resultBlankTx = useCase(
            fundId = fundId,
            isExpense = false,
            transactionId = "   ",
            originalAmount = 50_000L,
            voidReason = validReason,
            adminEmail = adminEmail
        )
        assertThat(resultBlankTx.isFailure).isTrue()
        assertThat(resultBlankTx.exceptionOrNull()).isInstanceOf(FundCircleError.NotFound.ResourceNotFound::class.java)
    }

    @Test
    fun invoke_whenValid_trimsInputAndDelegatesToRepository() = runTest {
        coEvery {
            transactionRepository.voidTransaction(
                fundId = fundId,
                isExpense = true,
                transactionId = txId,
                originalAmount = 25_000L,
                voidReason = validReason,
                adminEmail = adminEmail
            )
        } returns Result.success(Unit)

        val result = useCase(
            fundId = fundId,
            isExpense = true,
            transactionId = txId,
            originalAmount = 25_000L,
            voidReason = "   $validReason   ",
            adminEmail = "  $adminEmail  "
        )

        assertThat(result.isSuccess).isTrue()
        coVerify {
            transactionRepository.voidTransaction(
                fundId = fundId,
                isExpense = true,
                transactionId = txId,
                originalAmount = 25_000L,
                voidReason = validReason,
                adminEmail = adminEmail
            )
        }
    }
}
