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
}
