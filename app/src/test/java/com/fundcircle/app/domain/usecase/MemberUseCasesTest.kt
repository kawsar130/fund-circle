package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.MemberKyc
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MemberUseCasesTest {

    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private val fundId = "fund-test-101"
    private val userId = "usr-1"

    private lateinit var observeMemberUseCase: ObserveMemberUseCase
    private lateinit var observeCircleMembersUseCase: ObserveCircleMembersUseCase
    private lateinit var getMemberKycUseCase: GetMemberKycUseCase
    private lateinit var toggleMemberStatusUseCase: ToggleMemberStatusUseCase

    private val sampleMember = CircleMember(
        userId = userId,
        memberId = "FC-001",
        email = "alice@example.com",
        name = "Alice",
        phone = "01711111111",
        role = MemberRole.ADMIN,
        status = MemberStatus.ACTIVE,
        totalContributed = 200_000L,
        joinedAt = 1000L,
        lastActiveAt = 2000L
    )

    private val sampleKyc = MemberKyc(
        userId = userId,
        nidNumber = "19882692015000123",
        maskedNid = "•••• •••• 0123",
        nomineeName = "Nasrin Akter",
        nomineePhone = "01812345678",
        nomineeRelation = "Spouse"
    )

    @Before
    fun setUp() {
        observeMemberUseCase = ObserveMemberUseCase(memberRepository)
        observeCircleMembersUseCase = ObserveCircleMembersUseCase(memberRepository)
        getMemberKycUseCase = GetMemberKycUseCase(memberRepository)
        toggleMemberStatusUseCase = ToggleMemberStatusUseCase(memberRepository)
    }

    @Test
    fun observeMemberUseCase_delegatesToRepository() = runTest {
        every { memberRepository.observeMember(fundId, userId) } returns flowOf(sampleMember)

        val result = observeMemberUseCase(fundId, userId).first()

        assertThat(result).isNotNull()
        assertThat(result?.userId).isEqualTo(userId)
        assertThat(result?.name).isEqualTo("Alice")
    }

    @Test
    fun observeCircleMembersUseCase_delegatesToRepository() = runTest {
        every { memberRepository.observeMembers(fundId) } returns flowOf(listOf(sampleMember))

        val result = observeCircleMembersUseCase(fundId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].userId).isEqualTo(userId)
    }

    @Test
    fun getMemberKycUseCase_delegatesToRepository() = runTest {
        coEvery { memberRepository.getMemberKyc(fundId, userId) } returns Result.success(sampleKyc)

        val result = getMemberKycUseCase(fundId, userId)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().maskedNid).isEqualTo("•••• •••• 0123")
    }

    @Test
    fun toggleMemberStatusUseCase_delegatesToRepository() = runTest {
        coEvery { memberRepository.toggleMemberStatus(fundId, userId, MemberStatus.INACTIVE) } returns Result.success(Unit)

        val result = toggleMemberStatusUseCase(fundId, userId, MemberStatus.INACTIVE)

        assertThat(result.isSuccess).isTrue()
    }
}
