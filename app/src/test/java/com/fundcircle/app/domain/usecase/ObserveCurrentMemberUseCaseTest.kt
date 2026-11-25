package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveCurrentMemberUseCaseTest {

    private val memberRepository: MemberRepository = mockk()
    private val useCase = ObserveCurrentMemberUseCase(memberRepository)
    private val fundId = "fund-1"

    private fun member(docId: String, email: String) = CircleMember(
        userId = docId,
        memberId = "FC-00${docId.length}",
        email = email,
        name = "Name",
        phone = "01700000000",
        role = MemberRole.MEMBER,
        status = MemberStatus.ACTIVE,
        totalContributed = 0L,
        joinedAt = 1L,
        lastActiveAt = 1L
    )

    @Test
    fun creator_isResolvedByAuthUid() = runTest {
        val creator = member("auth-uid-1", "admin@example.com")
        every { memberRepository.observeMembers(fundId) } returns flowOf(listOf(creator))

        val result = useCase(fundId, uid = "auth-uid-1", email = "other@example.com").first()

        assertThat(result).isEqualTo(creator)
    }

    @Test
    fun invitedMember_whoseDocIdIsNotTheirUid_isResolvedByEmailIgnoringCase() = runTest {
        val invited = member("random-invite-uuid", "rahim@example.com")
        every { memberRepository.observeMembers(fundId) } returns
            flowOf(listOf(member("auth-uid-1", "admin@example.com"), invited))

        val result = useCase(fundId, uid = "rahims-auth-uid", email = "Rahim@Example.com").first()

        assertThat(result).isEqualTo(invited)
    }

    @Test
    fun nonMember_resolvesToNull() = runTest {
        every { memberRepository.observeMembers(fundId) } returns
            flowOf(listOf(member("auth-uid-1", "admin@example.com")))

        val result = useCase(fundId, uid = "stranger", email = "stranger@example.com").first()

        assertThat(result).isNull()
    }
}
