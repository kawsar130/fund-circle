package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircle
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LeaveCircleUseCaseTest {

    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private lateinit var useCase: LeaveCircleUseCase

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
        useCase = LeaveCircleUseCase(memberRepository)
    }

    @Test
    fun `active admin cannot leave circle without transferring ownership`() = runTest {
        val result = useCase(
            fundId = "fund-101",
            userId = "user-admin-1", // Active Admin!
            circle = sampleCircle
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        coVerify(exactly = 0) { memberRepository.toggleMemberStatus(any(), any(), any()) }
    }

    @Test
    fun `blank userId fails with IllegalArgumentException`() = runTest {
        val result = useCase(
            fundId = "fund-101",
            userId = "",
            circle = sampleCircle
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        coVerify(exactly = 0) { memberRepository.toggleMemberStatus(any(), any(), any()) }
    }

    @Test
    fun `stakeholder member successfully marks status as LEFT`() = runTest {
        coEvery {
            memberRepository.toggleMemberStatus("fund-101", "user-member-2", MemberStatus.LEFT)
        } returns Result.success(Unit)

        val result = useCase(
            fundId = "fund-101",
            userId = "user-member-2",
            circle = sampleCircle
        )

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            memberRepository.toggleMemberStatus("fund-101", "user-member-2", MemberStatus.LEFT)
        }
    }
}
