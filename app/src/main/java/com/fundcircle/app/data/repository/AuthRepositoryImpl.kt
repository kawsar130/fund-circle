package com.fundcircle.app.data.repository

import com.fundcircle.app.core.common.IoDispatcher
import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.data.datasource.remote.AuthRemoteDataSource
import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.UserSession
import com.fundcircle.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    override val userSession: Flow<UserSession?> = sessionLocalDataSource.observeUserSession()

    override suspend fun signInWithGoogle(idToken: String): Result<UserSession> = withContext(ioDispatcher) {
        runCatching {
            try {
                val session = authRemoteDataSource.signInWithGoogleCredential(idToken)
                val cachedActiveCircleId = sessionLocalDataSource.getActiveCircleId()
                val sessionWithCircle = session.copy(activeCircleId = cachedActiveCircleId)
                sessionLocalDataSource.saveUserSession(sessionWithCircle)
                sessionWithCircle
            } catch (e: FirebaseNetworkException) {
                throw FundCircleError.Network.Timeout
            } catch (e: FirebaseAuthException) {
                throw FundCircleError.Authorization.AuthenticationFailed(
                    e.message ?: "Authentication failed."
                )
            } catch (e: FundCircleError) {
                throw e
            } catch (e: Throwable) {
                throw FundCircleError.Authorization.AuthenticationFailed(
                    e.message ?: "Authentication failed."
                )
            }
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            authRemoteDataSource.signOut()
            sessionLocalDataSource.clearSession()
        }
    }

    override suspend fun getActiveCircleId(): String? = withContext(ioDispatcher) {
        sessionLocalDataSource.getActiveCircleId()
    }

    override suspend fun setActiveCircleId(fundId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            sessionLocalDataSource.setActiveCircleId(fundId)
        }
    }
}
