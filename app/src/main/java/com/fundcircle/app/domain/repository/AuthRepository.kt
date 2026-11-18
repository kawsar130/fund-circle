package com.fundcircle.app.domain.repository

import com.fundcircle.app.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository interface for authentication and session management
 * as specified in API Specification § 5.1.
 */
interface AuthRepository {
    /** Emits real-time authentication session state */
    val userSession: Flow<UserSession?>

    /** Authenticates using Google ID Token from Credential Manager */
    suspend fun signInWithGoogle(idToken: String): Result<UserSession>

    /** Clears session state and signs out from Firebase */
    suspend fun signOut(): Result<Unit>

    /** Retrieves the active circle ID cached in EncryptedSharedPreferences */
    suspend fun getActiveCircleId(): String?

    /** Persists the last viewed circle ID for fast-path launch */
    suspend fun setActiveCircleId(fundId: String): Result<Unit>
}
