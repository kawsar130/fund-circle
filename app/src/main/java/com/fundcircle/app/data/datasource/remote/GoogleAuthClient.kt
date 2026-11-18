package com.fundcircle.app.data.datasource.remote

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.fundcircle.app.BuildConfig
import com.fundcircle.app.R
import com.fundcircle.app.core.common.UiText
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result abstraction for Google Identity One-Tap Sign-In.
 */
sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failure(val error: UiText, val cause: Throwable? = null) : GoogleSignInResult
}

/**
 * Client wrapper around Android Credential Manager for Google Identity One-Tap.
 */
@Singleton
class GoogleAuthClient @Inject constructor(
    private val credentialManager: CredentialManager
) {
    suspend fun getGoogleIdToken(context: Context): GoogleSignInResult {
        return try {
            val serverClientId = BuildConfig.WEB_CLIENT_ID.trim()
            if (serverClientId.isBlank()) {
                return GoogleSignInResult.Failure(
                    error = UiText.StringResource(R.string.auth_error_sign_in_failed)
                )
            }

            val activity = context.findActivity()
                ?: return GoogleSignInResult.Failure(
                    error = UiText.StringResource(R.string.auth_error_sign_in_failed)
                )

            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(googleIdTokenCredential.idToken)
            } else {
                GoogleSignInResult.Failure(
                    error = UiText.StringResource(R.string.auth_error_sign_in_failed)
                )
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleSignInResult.Failure(
                error = UiText.StringResource(R.string.auth_error_no_accounts),
                cause = e
            )
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Failure(
                error = e.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.auth_error_sign_in_failed),
                cause = e
            )
        } catch (e: Exception) {
            GoogleSignInResult.Failure(
                error = e.message?.takeIf { it.isNotBlank() }?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.auth_error_sign_in_failed),
                cause = e
            )
        }
    }

    suspend fun clearCredentialState() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // Ignore failure when clearing credential state
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
