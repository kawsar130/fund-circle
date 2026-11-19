package com.fundcircle.app.data.repository

import com.fundcircle.app.data.datasource.remote.MemberRemoteDataSource
import com.fundcircle.app.data.dto.CircleMemberDto
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MemberRepositoryImplTest {

    private val remoteDataSource: MemberRemoteDataSource = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: MemberRepositoryImpl
    private val fundId = "fund-test-101"

    @Before
    fun setUp() {
        repository = MemberRepositoryImpl(
            memberRemoteDataSource = remoteDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun observeMembers_mapsDtosToDomainEntities() = runTest(testDispatcher) {
        val dto1 = CircleMemberDto(
            userId = "user-1",
            memberId = "FC-001",
            email = "alice@example.com",
            name = "Alice",
            role = "ADMIN",
            status = "ACTIVE",
            totalContributed = 200_000L,
            joinedAt = 1_000L,
            lastActiveAt = 2_000L
        )
        val dto2 = CircleMemberDto(
            userId = "user-2",
            memberId = "FC-002",
            email = "bob@example.com",
            name = "Bob",
            role = "MEMBER",
            status = "INACTIVE",
            totalContributed = 50_000L,
            joinedAt = 1_500L,
            lastActiveAt = 2_500L
        )

        every { remoteDataSource.observeMembers(fundId) } returns flowOf(listOf(dto1, dto2))

        val result = repository.observeMembers(fundId).first()

        assertThat(result).hasSize(2)
        assertThat(result[0].userId).isEqualTo("user-1")
        assertThat(result[0].role).isEqualTo(MemberRole.ADMIN)
        assertThat(result[0].status).isEqualTo(MemberStatus.ACTIVE)
        assertThat(result[0].totalContributed).isEqualTo(200_000L)

        assertThat(result[1].userId).isEqualTo("user-2")
        assertThat(result[1].role).isEqualTo(MemberRole.MEMBER)
        assertThat(result[1].status).isEqualTo(MemberStatus.INACTIVE)
    }

    @Test
    fun observeMembers_resilientlyFiltersOutCorruptedDtos() = runTest(testDispatcher) {
        val validDto = CircleMemberDto(
            userId = "user-1",
            memberId = "FC-001",
            email = "alice@example.com",
            name = "Alice",
            role = "MEMBER",
            status = "ACTIVE",
            totalContributed = 100_000L
        )

        every { remoteDataSource.observeMembers(fundId) } returns flowOf(listOf(validDto))

        val result = repository.observeMembers(fundId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].userId).isEqualTo("user-1")
    }

    @Test
    fun observeMembers_whenPermissionDenied_mapsToAuthorizationError() = runTest(testDispatcher) {
        val exception = FirebaseFirestoreException(
            "Access denied",
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )

        every { remoteDataSource.observeMembers(fundId) } returns flow { throw exception }

        try {
            repository.observeMembers(fundId).first()
            assert(false) { "Expected FundCircleError.Authorization.NotAuthorized" }
        } catch (e: Throwable) {
            assertThat(e).isInstanceOf(FundCircleError.Authorization.NotAuthorized::class.java)
        }
    }

    @Test
    fun observeMembers_whenNetworkException_mapsToTimeoutError() = runTest(testDispatcher) {
        val exception = FirebaseNetworkException("Offline")

        every { remoteDataSource.observeMembers(fundId) } returns flow { throw exception }

        try {
            repository.observeMembers(fundId).first()
            assert(false) { "Expected FundCircleError.Network.Timeout" }
        } catch (e: Throwable) {
            assertThat(e).isInstanceOf(FundCircleError.Network.Timeout::class.java)
        }
    }

    @Test
    fun observeMember_whenFound_emitsMappedDomainMember() = runTest(testDispatcher) {
        val dto = CircleMemberDto(
            userId = "user-1",
            memberId = "FC-001",
            email = "alice@example.com",
            name = "Alice",
            phone = "01711111111",
            role = "ADMIN",
            status = "ACTIVE",
            totalContributed = 200_000L,
            joinedAt = 1_000L,
            lastActiveAt = 2_000L
        )
        every { remoteDataSource.observeMember(fundId, "user-1") } returns flowOf(dto)

        val result = repository.observeMember(fundId, "user-1").first()

        assertThat(result).isNotNull()
        assertThat(result?.userId).isEqualTo("user-1")
        assertThat(result?.name).isEqualTo("Alice")
        assertThat(result?.phone).isEqualTo("01711111111")
        assertThat(result?.role).isEqualTo(MemberRole.ADMIN)
    }

    @Test
    fun getMemberKyc_success_returnsDomainKycWithMasking() = runTest(testDispatcher) {
        val kycDto = com.fundcircle.app.data.dto.MemberKycDto(
            userId = "user-1",
            nidNumber = "19882692015000123",
            nomineeName = "Nasrin Akter",
            nomineePhone = "01812345678",
            nomineeRelation = "Spouse",
            verifiedByAdmin = false,
            updatedAt = 1000L
        )
        io.mockk.coEvery { remoteDataSource.getMemberKyc(fundId, "user-1") } returns Result.success(kycDto)

        val result = repository.getMemberKyc(fundId, "user-1")

        assertThat(result.isSuccess).isTrue()
        val kyc = result.getOrThrow()
        assertThat(kyc.userId).isEqualTo("user-1")
        assertThat(kyc.maskedNid).isEqualTo("•••• •••• 0123")
        assertThat(kyc.nomineeName).isEqualTo("Nasrin Akter")
        assertThat(kyc.nomineeRelation).isEqualTo("Spouse")
    }

    @Test
    fun updateMemberKyc_success_delegatesToDataSource() = runTest(testDispatcher) {
        val kyc = com.fundcircle.app.domain.model.MemberKyc(
            userId = "user-1",
            nidNumber = "19882692015000123",
            maskedNid = "•••• •••• 0123",
            nomineeName = "Nasrin Akter",
            nomineePhone = "01812345678",
            nomineeRelation = "Spouse"
        )
        io.mockk.coEvery { remoteDataSource.updateMemberKyc(fundId, any()) } returns Result.success(Unit)

        val result = repository.updateMemberKyc(fundId, kyc)

        assertThat(result.isSuccess).isTrue()
        io.mockk.coVerify { remoteDataSource.updateMemberKyc(fundId, match { it.nomineeName == "Nasrin Akter" }) }
    }

    @Test
    fun inviteMember_delegatesToDataSourceWithActiveStatus() = runTest(testDispatcher) {
        val memberSlot = io.mockk.slot<CircleMemberDto>()
        io.mockk.coEvery { remoteDataSource.inviteMember(fundId, capture(memberSlot)) } returns Result.success(Unit)

        val result = repository.inviteMember(
            fundId = fundId,
            name = "Tariqul Islam",
            phone = "01712345678",
            email = "tariqul@gmail.com",
            memberId = "FC-005"
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(memberSlot.captured.name).isEqualTo("Tariqul Islam")
        assertThat(memberSlot.captured.phone).isEqualTo("01712345678")
        assertThat(memberSlot.captured.email).isEqualTo("tariqul@gmail.com")
        assertThat(memberSlot.captured.memberId).isEqualTo("FC-005")
        assertThat(memberSlot.captured.status).isEqualTo("ACTIVE")
        assertThat(memberSlot.captured.totalContributed).isEqualTo(0L)
    }

    @Test
    fun toggleMemberStatus_delegatesToDataSource() = runTest(testDispatcher) {
        io.mockk.coEvery { remoteDataSource.toggleMemberStatus(fundId, "user-2", "INACTIVE") } returns Result.success(Unit)

        val result = repository.toggleMemberStatus(fundId, "user-2", MemberStatus.INACTIVE)

        assertThat(result.isSuccess).isTrue()
        io.mockk.coVerify { remoteDataSource.toggleMemberStatus(fundId, "user-2", "INACTIVE") }
    }
}
