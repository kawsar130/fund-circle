package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.repository.FundRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateCircleUseCaseTest {

    private val fundRepository: FundRepository = mockk(relaxed = true)
    private lateinit var useCase: CreateCircleUseCase

    @Before
    fun setUp() {
        useCase = CreateCircleUseCase(fundRepository)
        every { fundRepository.observeUserCircles() } returns flowOf(emptyList())
    }

    @Test
    fun createCircle_nameTooShort_returnsInvalidName() = runTest {
        val result = useCase(name = "AB", currency = Currency.BDT, description = null)

        assertThat(result.isFailure).isTrue()
        val error = result.exceptionOrNull()
        assertThat(error).isInstanceOf(FundCircleError.Validation.InvalidName::class.java)
        assertThat((error as FundCircleError.Validation.InvalidName).length).isEqualTo(2)
        coVerify(exactly = 0) { fundRepository.createCircle(any(), any(), any()) }
    }

    @Test
    fun createCircle_nameTooLong_returnsInvalidName() = runTest {
        val longName = "A".repeat(51)
        val result = useCase(name = longName, currency = Currency.BDT, description = null)

        assertThat(result.isFailure).isTrue()
        val error = result.exceptionOrNull()
        assertThat(error).isInstanceOf(FundCircleError.Validation.InvalidName::class.java)
        assertThat((error as FundCircleError.Validation.InvalidName).length).isEqualTo(51)
        coVerify(exactly = 0) { fundRepository.createCircle(any(), any(), any()) }
    }

    @Test
    fun createCircle_descriptionTooLong_returnsDescriptionTooLong() = runTest {
        val longDesc = "D".repeat(251)
        val result = useCase(name = "Valid Circle", currency = Currency.USD, description = longDesc)

        assertThat(result.isFailure).isTrue()
        val error = result.exceptionOrNull()
        assertThat(error).isInstanceOf(FundCircleError.Validation.DescriptionTooLong::class.java)
        assertThat((error as FundCircleError.Validation.DescriptionTooLong).length).isEqualTo(251)
        coVerify(exactly = 0) { fundRepository.createCircle(any(), any(), any()) }
    }

    @Test
    fun createCircle_when5CirclesExist_returnsCircleLimitReached() = runTest {
        val existingCircles = List(5) { i ->
            FundCircle(
                id = "id-$i",
                name = "Circle $i",
                description = null,
                currency = Currency.BDT,
                adminId = "admin",
                adminEmail = "admin@example.com",
                memberEmails = listOf("admin@example.com"),
                memberCount = 1,
                totalDeposits = 0L,
                totalExpenses = 0L,
                recordedBalance = 0L,
                isLocked = false,
                createdAt = 0L
            )
        }
        every { fundRepository.observeUserCircles() } returns flowOf(existingCircles)

        val result = useCase(name = "Sixth Circle", currency = Currency.BDT, description = null)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Business.CircleLimitReached::class.java)
        coVerify(exactly = 0) { fundRepository.createCircle(any(), any(), any()) }
    }

    @Test
    fun createCircle_validInput_trimsAndDelegatesToRepository() = runTest {
        coEvery {
            fundRepository.createCircle("Apex Syndicate", Currency.EUR, "Pooled fund")
        } returns Result.success("new-fund-uuid")

        val result = useCase(
            name = "   Apex Syndicate   ",
            currency = Currency.EUR,
            description = "   Pooled fund   "
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("new-fund-uuid")
        coVerify(exactly = 1) {
            fundRepository.createCircle("Apex Syndicate", Currency.EUR, "Pooled fund")
        }
    }

    @Test
    fun createCircle_blankDescription_normalizesToNull() = runTest {
        coEvery {
            fundRepository.createCircle("Valid Name", Currency.BDT, null)
        } returns Result.success("fund-123")

        val result = useCase(
            name = "Valid Name",
            currency = Currency.BDT,
            description = "    "
        )

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            fundRepository.createCircle("Valid Name", Currency.BDT, null)
        }
    }

    @Test(expected = kotlinx.coroutines.CancellationException::class)
    fun createCircle_cancellationException_rethrowsWithoutWrapping() = runTest {
        coEvery { fundRepository.createCircle(any(), any(), any()) } throws kotlinx.coroutines.CancellationException("Cancelled")

        useCase(
            name = "Valid Name",
            currency = Currency.BDT,
            description = null
        )
    }
}
