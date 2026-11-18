package com.fundcircle.app.data.repository

import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.data.datasource.remote.FundRemoteDataSource
import com.fundcircle.app.data.dto.FundCircleDto
import com.fundcircle.app.data.dto.MemberDto
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.UserSession
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class FundRepositoryImplTest {

    private val fundRemoteDataSource: FundRemoteDataSource = mockk(relaxed = true)
    private val sessionLocalDataSource: SessionLocalDataSource = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: FundRepositoryImpl

    private val sampleSession = UserSession(
        uid = "usr_101",
        email = "kawsar@fundcircle.app",
        displayName = "Kawsar",
        photoUrl = null,
        idToken = "valid-token",
        activeCircleId = null
    )

    @Before
    fun setUp() {
        repository = FundRepositoryImpl(
            fundRemoteDataSource = fundRemoteDataSource,
            sessionLocalDataSource = sessionLocalDataSource,
            ioDispatcher = testDispatcher
        )
        every { sessionLocalDataSource.getUserSession() } returns sampleSession
        every { sessionLocalDataSource.observeUserSession() } returns flowOf(sampleSession)
    }

    @Test
    fun createCircle_success_initializes0LAggregatesAndAdminMemberAndGeneratesUUID() = runTest {
        val circleSlot = slot<FundCircleDto>()
        val memberSlot = slot<MemberDto>()

        coEvery { fundRemoteDataSource.observeUserCircles(sampleSession.email) } returns flowOf(emptyList())
        coEvery { fundRemoteDataSource.createCircle(capture(circleSlot), capture(memberSlot)) } returns Result.success(Unit)

        val result = repository.createCircle(
            name = "Apex Growth Syndicate",
            currency = Currency.BDT,
            description = "Commercial property investment"
        )

        assertThat(result.isSuccess).isTrue()
        val fundId = result.getOrNull()
        assertThat(fundId).isNotNull()
        // Must be valid UUID
        UUID.fromString(fundId!!)

        val capturedCircle = circleSlot.captured
        assertThat(capturedCircle.fundId).isEqualTo(fundId)
        assertThat(capturedCircle.name).isEqualTo("Apex Growth Syndicate")
        assertThat(capturedCircle.description).isEqualTo("Commercial property investment")
        assertThat(capturedCircle.currency).isEqualTo("BDT")
        assertThat(capturedCircle.adminId).isEqualTo(sampleSession.uid)
        assertThat(capturedCircle.adminEmail).isEqualTo(sampleSession.email)
        assertThat(capturedCircle.memberEmails).containsExactly(sampleSession.email)
        assertThat(capturedCircle.memberCount).isEqualTo(1)
        // Invariant 1: initial aggregates strictly 0L
        assertThat(capturedCircle.totalDeposits).isEqualTo(0L)
        assertThat(capturedCircle.totalExpenses).isEqualTo(0L)
        assertThat(capturedCircle.recordedBalance).isEqualTo(0L)
        assertThat(capturedCircle.isLocked).isFalse()

        val capturedMember = memberSlot.captured
        assertThat(capturedMember.userId).isEqualTo(sampleSession.uid)
        assertThat(capturedMember.memberId).isEqualTo("FC-001")
        assertThat(capturedMember.email).isEqualTo(sampleSession.email)
        assertThat(capturedMember.name).isEqualTo(sampleSession.displayName)
        assertThat(capturedMember.role).isEqualTo(MemberRole.ADMIN.name)
        assertThat(capturedMember.status).isEqualTo("ACTIVE")
        assertThat(capturedMember.totalContributed).isEqualTo(0L)
    }

    @Test
    fun createCircle_when5CirclesExist_returnsCircleLimitReached() = runTest {
        val existing5Circles = List(5) { index ->
            FundCircleDto(
                fundId = "fund-$index",
                name = "Circle $index",
                currency = "BDT",
                adminId = sampleSession.uid,
                adminEmail = sampleSession.email,
                memberEmails = listOf(sampleSession.email)
            )
        }
        coEvery { fundRemoteDataSource.observeUserCircles(sampleSession.email) } returns flowOf(existing5Circles)

        val result = repository.createCircle(
            name = "Circle Six",
            currency = Currency.USD,
            description = null
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Business.CircleLimitReached::class.java)

        coVerify(exactly = 0) {
            fundRemoteDataSource.createCircle(any(), any())
        }
    }

    @Test
    fun createCircle_whenNoSession_returnsSessionExpired() = runTest {
        every { sessionLocalDataSource.getUserSession() } returns null

        val result = repository.createCircle(
            name = "Unauthenticated Circle",
            currency = Currency.EUR,
            description = null
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Authorization.SessionExpired::class.java)
    }

    @Test
    fun observeUserCircles_mapsDtoToDomain() = runTest {
        val dtos = listOf(
            FundCircleDto(
                fundId = "f-1",
                name = "Circle 1",
                currency = "BDT",
                adminId = sampleSession.uid,
                adminEmail = sampleSession.email,
                memberEmails = listOf(sampleSession.email),
                memberCount = 3,
                totalDeposits = 100000L,
                totalExpenses = 20000L,
                recordedBalance = 80000L,
                isLocked = false,
                createdAt = 1000L
            )
        )
        every { fundRemoteDataSource.observeUserCircles(sampleSession.email) } returns flowOf(dtos)

        val domainCircles = repository.observeUserCircles().first()

        assertThat(domainCircles).hasSize(1)
        val circle = domainCircles[0]
        assertThat(circle.id).isEqualTo("f-1")
        assertThat(circle.name).isEqualTo("Circle 1")
        assertThat(circle.currency).isEqualTo(Currency.BDT)
        assertThat(circle.recordedBalance).isEqualTo(80000L)
    }

    @Test
    fun observeCircle_success_returnsDomainModel() = runTest {
        val dto = FundCircleDto(
            fundId = "f-abc",
            name = "Circle ABC",
            currency = "USD",
            adminId = sampleSession.uid,
            adminEmail = sampleSession.email,
            memberEmails = listOf(sampleSession.email),
            memberCount = 5,
            totalDeposits = 50000L,
            totalExpenses = 5000L,
            recordedBalance = 45000L,
            isLocked = true,
            createdAt = 2000L
        )
        every { fundRemoteDataSource.observeCircle("f-abc") } returns flowOf(dto)

        val domain = repository.observeCircle("f-abc").first()

        assertThat(domain.id).isEqualTo("f-abc")
        assertThat(domain.currency).isEqualTo(Currency.USD)
        assertThat(domain.isLocked).isTrue()
    }

    @Test(expected = FundCircleError.NotFound.ResourceNotFound::class)
    fun observeCircle_notFound_throwsResourceNotFound() = runTest {
        every { fundRemoteDataSource.observeCircle("missing-id") } returns flowOf(null)

        repository.observeCircle("missing-id").first()
    }

    @Test
    fun toggleCircleLock_delegatesToRemoteDataSource() = runTest {
        coEvery { fundRemoteDataSource.toggleCircleLock("f-123", true) } returns Result.success(Unit)

        val result = repository.toggleCircleLock("f-123", true)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) {
            fundRemoteDataSource.toggleCircleLock("f-123", true)
        }
    }

    @Test
    fun createCircle_networkException_mapsToTimeout() = runTest {
        val networkException = com.google.firebase.FirebaseNetworkException("Network disconnected")
        coEvery { fundRemoteDataSource.observeUserCircles(sampleSession.email) } returns flowOf(emptyList())
        coEvery { fundRemoteDataSource.createCircle(any(), any()) } returns Result.failure(networkException)

        val result = repository.createCircle(
            name = "Test Circle",
            currency = Currency.BDT,
            description = null
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Network.Timeout::class.java)
    }

    @Test
    fun createCircle_permissionDenied_mapsToNotAuthorized() = runTest {
        val permissionDenied = mockk<com.google.firebase.firestore.FirebaseFirestoreException>()
        every { permissionDenied.code } returns com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
        every { permissionDenied.message } returns "Permission denied"
        coEvery { fundRemoteDataSource.observeUserCircles(sampleSession.email) } returns flowOf(emptyList())
        coEvery { fundRemoteDataSource.createCircle(any(), any()) } returns Result.failure(permissionDenied)

        val result = repository.createCircle(
            name = "Test Circle",
            currency = Currency.BDT,
            description = null
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Authorization.NotAuthorized::class.java)
    }

    @Test(expected = kotlinx.coroutines.CancellationException::class)
    fun createCircle_cancellationException_rethrowsWithoutWrapping() = runTest {
        coEvery { fundRemoteDataSource.observeUserCircles(sampleSession.email) } returns flowOf(emptyList())
        coEvery { fundRemoteDataSource.createCircle(any(), any()) } throws kotlinx.coroutines.CancellationException("Cancelled")

        repository.createCircle("Name", Currency.BDT, null)
    }
}
