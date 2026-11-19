package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.CircleMember
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.domain.repository.MemberRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddMemberUseCaseTest {

    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private lateinit var useCase: AddMemberUseCase
    private val fundId = "fund-test-101"

    @Before
    fun setUp() {
        useCase = AddMemberUseCase(memberRepository)
    }

    @Test
    fun computeNextMemberId_emptyList_returnsFC001() {
        val nextId = AddMemberUseCase.computeNextMemberId(emptyList())
        assertThat(nextId).isEqualTo("FC-001")
    }

    @Test
    fun computeNextMemberId_sequentialList_returnsIncrementedId() {
        val members = listOf(
            createMember("FC-001"),
            createMember("FC-002"),
            createMember("FC-003")
        )
        val nextId = AddMemberUseCase.computeNextMemberId(members)
        assertThat(nextId).isEqualTo("FC-004")
    }

    @Test
    fun computeNextMemberId_nonContiguousList_takesMaxPlusOne() {
        val members = listOf(
            createMember("FC-001"),
            createMember("FC-009")
        )
        val nextId = AddMemberUseCase.computeNextMemberId(members)
        assertThat(nextId).isEqualTo("FC-010")
    }

    @Test
    fun invoke_whenNameIsBlank_returnsMemberNameRequired() = runTest {
        val result = useCase(
            fundId = fundId,
            name = "   ",
            phone = "01712345678",
            email = "test@gmail.com"
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.MemberNameRequired::class.java)
    }

    @Test
    fun invoke_whenNameIsTooShort_returnsMemberNameRequired() = runTest {
        val result = useCase(
            fundId = fundId,
            name = "A",
            phone = "01712345678",
            email = "test@gmail.com"
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.MemberNameRequired::class.java)
    }

    @Test
    fun invoke_whenPhoneHasLessThan11Digits_returnsInvalidPhone() = runTest {
        val result = useCase(
            fundId = fundId,
            name = "Valid Name",
            phone = "0171234567",
            email = "test@gmail.com"
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidPhone::class.java)
    }

    @Test
    fun invoke_whenPhoneHasNonDigits_returnsInvalidPhone() = runTest {
        val result = useCase(
            fundId = fundId,
            name = "Valid Name",
            phone = "0171234567A",
            email = "test@gmail.com"
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidPhone::class.java)
    }

    @Test
    fun invoke_whenEmailIsInvalid_returnsInvalidEmail() = runTest {
        val result = useCase(
            fundId = fundId,
            name = "Valid Name",
            phone = "01712345678",
            email = "not-an-email"
        )
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Validation.InvalidEmail::class.java)
    }

    @Test
    fun invoke_validInputs_allocatesNextMemberIdAndInvites() = runTest {
        every { memberRepository.observeMembers(fundId) } returns flowOf(
            listOf(createMember("FC-001"), createMember("FC-002"))
        )
        coEvery {
            memberRepository.inviteMember(fundId, "Tariqul Islam", "01712345678", "tariqul@gmail.com", "FC-003")
        } returns Result.success(Unit)

        val result = useCase(
            fundId = fundId,
            name = "Tariqul Islam",
            phone = "01712345678",
            email = "tariqul@gmail.com"
        )

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun invoke_whenEmailAlreadyExists_returnsDuplicateEntry() = runTest {
        every { memberRepository.observeMembers(fundId) } returns flowOf(
            listOf(createMember("FC-001").copy(email = "existing@gmail.com"))
        )

        val result = useCase(
            fundId = fundId,
            name = "New Member",
            phone = "01712345678",
            email = "existing@gmail.com"
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Conflict.DuplicateEntry::class.java)
    }

    private fun createMember(memberId: String) = CircleMember(
        userId = "usr-$memberId",
        memberId = memberId,
        email = "member@example.com",
        name = "Member $memberId",
        role = MemberRole.MEMBER,
        status = MemberStatus.ACTIVE,
        totalContributed = 100_000L,
        joinedAt = 1000L,
        lastActiveAt = 2000L
    )
}
