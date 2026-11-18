package com.fundcircle.app.domain.usecase

import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)

    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var getActiveCircleUseCase: GetActiveCircleUseCase
    private lateinit var setActiveCircleUseCase: SetActiveCircleUseCase

    private val sampleSession = UserSession(
        uid = "test-uid-123",
        email = "test@fundcircle.app",
        displayName = "Test User",
        photoUrl = "https://example.com/avatar.png",
        idToken = "sample-google-id-token",
        activeCircleId = "circle-xyz"
    )

    @Before
    fun setUp() {
        signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository)
        observeAuthStateUseCase = ObserveAuthStateUseCase(authRepository)
        signOutUseCase = SignOutUseCase(authRepository)
        getActiveCircleUseCase = GetActiveCircleUseCase(authRepository)
        setActiveCircleUseCase = SetActiveCircleUseCase(authRepository)
    }

    @Test
    fun signInWithGoogleUseCase_blankToken_returnsFailureWithoutCallingRepository() = runTest {
        val result = signInWithGoogleUseCase("   ")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        coVerify(exactly = 0) { authRepository.signInWithGoogle(any()) }
    }

    @Test
    fun signInWithGoogleUseCase_validToken_callsRepositoryAndReturnsSuccess() = runTest {
        coEvery { authRepository.signInWithGoogle("valid-token") } returns Result.success(sampleSession)

        val result = signInWithGoogleUseCase("valid-token")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(sampleSession)
        coVerify(exactly = 1) { authRepository.signInWithGoogle("valid-token") }
    }

    @Test
    fun observeAuthStateUseCase_emitsRepositorySessionFlow() = runTest {
        every { authRepository.userSession } returns flowOf(sampleSession)

        val flow = observeAuthStateUseCase()

        flow.collect { session ->
            assertThat(session).isEqualTo(sampleSession)
        }
    }

    @Test
    fun signOutUseCase_callsRepositorySignOut() = runTest {
        coEvery { authRepository.signOut() } returns Result.success(Unit)

        val result = signOutUseCase()

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun getActiveCircleUseCase_returnsRepositoryActiveCircleId() = runTest {
        coEvery { authRepository.getActiveCircleId() } returns "circle-xyz"

        val activeCircleId = getActiveCircleUseCase()

        assertThat(activeCircleId).isEqualTo("circle-xyz")
        coVerify(exactly = 1) { authRepository.getActiveCircleId() }
    }

    @Test
    fun setActiveCircleUseCase_blankCircleId_returnsFailure() = runTest {
        val result = setActiveCircleUseCase("")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        coVerify(exactly = 0) { authRepository.setActiveCircleId(any()) }
    }

    @Test
    fun setActiveCircleUseCase_validCircleId_callsRepository() = runTest {
        coEvery { authRepository.setActiveCircleId("circle-123") } returns Result.success(Unit)

        val result = setActiveCircleUseCase("circle-123")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { authRepository.setActiveCircleId("circle-123") }
    }
}
