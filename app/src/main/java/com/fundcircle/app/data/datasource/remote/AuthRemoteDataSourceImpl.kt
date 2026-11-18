package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.domain.model.FundCircleError
import com.fundcircle.app.domain.model.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val googleAuthClient: GoogleAuthClient
) : AuthRemoteDataSource {

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override fun observeAuthState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signInWithGoogleCredential(idToken: String): UserSession {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user
            ?: throw FundCircleError.Authorization.AuthenticationFailed("Firebase user is null after sign in.")

        val firebaseAuthToken = try {
            user.getIdToken(false).await()?.token ?: idToken
        } catch (_: Exception) {
            idToken
        }

        val userSession = UserSession(
            uid = user.uid,
            email = user.email ?: "",
            displayName = user.displayName ?: "",
            photoUrl = user.photoUrl?.toString(),
            idToken = firebaseAuthToken,
            activeCircleId = null
        )

        // Sync to root /users/{userId} per API Spec § 4.8 and CUJ-01
        syncUserProfile(user.uid, user.email ?: "", user.displayName ?: "", user.photoUrl?.toString())

        return userSession
    }

    override suspend fun signOut() {
        googleAuthClient.clearCredentialState()
        firebaseAuth.signOut()
    }

    private suspend fun syncUserProfile(
        uid: String,
        email: String,
        displayName: String,
        photoUrl: String?
    ) {
        try {
            val userDocRef = firestore.collection("users").document(uid)
            val snapshot = userDocRef.get().await()
            val now = System.currentTimeMillis()

            if (!snapshot.exists()) {
                val initialData = mapOf(
                    "userId" to uid,
                    "email" to email,
                    "displayName" to displayName,
                    "photoUrl" to photoUrl,
                    "createdCircleCount" to 0L,
                    "createdAt" to now,
                    "lastActiveAt" to now
                )
                userDocRef.set(initialData).await()
            } else {
                val updateData = mapOf(
                    "email" to email,
                    "displayName" to displayName,
                    "photoUrl" to photoUrl,
                    "lastActiveAt" to now
                )
                userDocRef.set(updateData, SetOptions.merge()).await()
            }
        } catch (_: Exception) {
            // Best effort sync: local writes will queue in 100MB Firestore cache
        }
    }
}
