package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Remote data source contract for Firebase Authentication and User Profile syncing.
 */
interface AuthRemoteDataSource {
    val currentUserId: String?
    fun observeAuthState(): Flow<String?>
    suspend fun signInWithGoogleCredential(idToken: String): UserSession
    suspend fun signOut()
}
