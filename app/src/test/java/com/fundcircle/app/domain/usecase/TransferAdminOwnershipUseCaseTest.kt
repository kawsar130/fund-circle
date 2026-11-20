package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.repository.FundRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TransferAdminOwnershipUseCaseTest {

    private val fundRepository: FundRepository = mockk(relaxed = true)
    private lateinit var useCase: TransferAdminOwnershipUseCase

    private val sampleCircle = FundCircle(
        id = "fund-101",
        name = "Green Valley Syndicate",
        currency = Currency.BDT,
        description = null,
        adminId = "user-admin-1",
        adminEmail = "admin@fundcircle.app",
        memberEmails = listOf("admin@fundcircle.app", "member@fundcircle.app"),
        memberCount = 2,
        totalDeposits = 500000L,
        totalExpenses = 20000L,
        recordedBalance = 480000L,
        isLocked = false,
        createdAt = 1700000000000L
    )

    @Before
    fun setUp() {
        useCase = TransferAdminOwnershipUseCase(fundRepository)
    }

    @Test
    fun `non-admin caller cannot transfer admin ownership`() = runTest {
        val result = useCase(
            fundId = "fund-101",
            currentAdminUserId = "user-member-2", // Not the admin!
            newAdminUserId = "user-member-3",
            newAdminEmail = "newadmin@fundcircle.app",
            circle = sampleCircle
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        coVerify(exactly = 0) { fundRepository.transferAdminRole(any(), any(), any(), any()) }
    }

    @Test
    fun `transferring to self fails with IllegalArgumentException`() = runTest {
        val result = useCase(
            fundId = "fund-101",
            currentAdminUserId = "user-admin-1",
            newAdminUserId = "user-admin-1", // Same user!
            newAdminEmail = "admin@fundcircle.app",
            circle = sampleCircle
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        coVerify(exactly = 0) { fundRepository.transferAdminRole(any(), any(), any(), any()) }
    }

    @Test
    fun `blank target member fails with IllegalArgumentException`() = runTest {
        val result = useCase(
            fundId = "fund-101",
            currentAdminUserId = "user-admin-1",
            newAdminUserId = "",
            newAdminEmail = "member@fundcircle.app",
            circle = sampleCircle
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        coVerify(exactly = 0) { fundRepository.transferAdminRole(any(), any(), any(), any()) }
    }

    @Test
    fun `blank target email fails with IllegalArgumentException`() = runTest {
        val result = useCase(
            fundId = "fund-101",
            currentAdminUserId = "user-admin-1",
            newAdminUserId = "user-member-2",
            newAdminEmail = "",
            circle = sampleCircle
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        coVerify(exactly = 0) { fundRepository.transferAdminRole(any(), any(), any(), any()) }
    }

    @Test
    fun `valid admin caller successfully delegates to fundRepository`() = runTest {
        coEvery {
            fundRepository.transferAdminRole(
                fundId = "fund-101",
                currentAdminUserId = "user-admin-1",
                newAdminUserId = "user-member-2",
                newAdminEmail = "member@fundcircle.app"
            )
        } returns Result.success(Unit)

        val result = useCase(
            fundId = "fund-101",
            currentAdminUserId = "user-admin-1",
            newAdminUserId = "user-member-2",
            newAdminEmail = "member@fundcircle.app",
            circle = sampleCircle
        )

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            fundRepository.transferAdminRole(
                fundId = "fund-101",
                currentAdminUserId = "user-admin-1",
                newAdminUserId = "user-member-2",
                newAdminEmail = "member@fundcircle.app"
            )
        }
    }
}
