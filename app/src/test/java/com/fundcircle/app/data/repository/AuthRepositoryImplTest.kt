package com.fundcircle.app.data.repository

import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.data.datasource.remote.AuthRemoteDataSource
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.UserSession
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private val authRemoteDataSource: AuthRemoteDataSource = mockk(relaxed = true)
    private val sessionLocalDataSource: SessionLocalDataSource = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: AuthRepositoryImpl

    private val remoteSession = UserSession(
        uid = "uid-101",
        email = "test@fundcircle.app",
        displayName = "Investor A",
        photoUrl = "https://example.com/a.jpg",
        idToken = "raw-id-token",
        activeCircleId = null
    )

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(
            authRemoteDataSource = authRemoteDataSource,
            sessionLocalDataSource = sessionLocalDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun userSession_observesSessionLocalDataSource() = runTest {
        every { sessionLocalDataSource.observeUserSession() } returns flowOf(remoteSession)

        repository.userSession.collect { session ->
            assertThat(session).isEqualTo(remoteSession)
        }
    }

    @Test
    fun signInWithGoogle_success_persistsSessionWithCachedCircleId() = runTest {
        coEvery { authRemoteDataSource.signInWithGoogleCredential("token-123") } returns remoteSession
        every { sessionLocalDataSource.getActiveCircleId() } returns "circle-cached-555"

        val result = repository.signInWithGoogle("token-123")

        assertThat(result.isSuccess).isTrue()
        val session = result.getOrNull()
        assertThat(session).isNotNull()
        assertThat(session?.activeCircleId).isEqualTo("circle-cached-555")

        verify(exactly = 1) {
            sessionLocalDataSource.saveUserSession(session!!)
        }
    }

    @Test
    fun signInWithGoogle_networkException_mapsToNetworkTimeoutError() = runTest {
        val networkException = mockk<FirebaseNetworkException>()
        every { networkException.message } returns "Network error"
        coEvery { authRemoteDataSource.signInWithGoogleCredential(any()) } throws networkException

        val result = repository.signInWithGoogle("token-123")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FundCircleError.Network.Timeout::class.java)
    }

    @Test
    fun signInWithGoogle_firebaseAuthException_mapsToAuthenticationFailed() = runTest {
        val authException = mockk<FirebaseAuthInvalidCredentialsException>()
        every { authException.message } returns "Invalid token"
        coEvery { authRemoteDataSource.signInWithGoogleCredential(any()) } throws authException

        val result = repository.signInWithGoogle("token-123")

        assertThat(result.isFailure).isTrue()
        val error = result.exceptionOrNull()
        assertThat(error).isInstanceOf(FundCircleError.Authorization.AuthenticationFailed::class.java)
    }

    @Test
    fun signOut_invokesRemoteAndClearsLocalSession() = runTest {
        val result = repository.signOut()

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { authRemoteDataSource.signOut() }
        verify(exactly = 1) { sessionLocalDataSource.clearSession() }
    }

    @Test
    fun getActiveCircleId_delegatesToLocalDataSource() = runTest {
        every { sessionLocalDataSource.getActiveCircleId() } returns "fund-target-123"

        val activeId = repository.getActiveCircleId()

        assertThat(activeId).isEqualTo("fund-target-123")
    }

    @Test
    fun setActiveCircleId_delegatesToLocalDataSource() = runTest {
        val result = repository.setActiveCircleId("fund-target-123")

        assertThat(result.isSuccess).isTrue()
        verify(exactly = 1) { sessionLocalDataSource.setActiveCircleId("fund-target-123") }
    }
}
