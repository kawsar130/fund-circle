package com.fundcircle.app.data.datasource.local

import android.content.SharedPreferences
import com.fundcircle.app.core.security.SessionKeys
import com.fundcircle.app.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionLocalDataSourceImpl @Inject constructor(
    private val prefs: SharedPreferences
) : SessionLocalDataSource {

    private val _sessionFlow = MutableStateFlow<UserSession?>(loadSessionFromPrefs())

    override fun getUserSession(): UserSession? = _sessionFlow.value

    override fun observeUserSession(): Flow<UserSession?> = _sessionFlow.asStateFlow()

    override fun saveUserSession(session: UserSession) {
        val editor = prefs.edit()
            .putString(SessionKeys.KEY_AUTH_TOKEN, session.idToken)
            .putString(SessionKeys.KEY_USER_UID, session.uid)
            .putString(SessionKeys.KEY_USER_EMAIL, session.email)
            .putString(SessionKeys.KEY_DISPLAY_NAME, session.displayName)

        if (session.photoUrl != null) {
            editor.putString(SessionKeys.KEY_PHOTO_URL, session.photoUrl)
        } else {
            editor.remove(SessionKeys.KEY_PHOTO_URL)
        }

        if (session.activeCircleId != null) {
            editor.putString(SessionKeys.KEY_ACTIVE_CIRCLE_ID, session.activeCircleId)
        } else {
            editor.remove(SessionKeys.KEY_ACTIVE_CIRCLE_ID)
        }
        editor.apply()

        _sessionFlow.value = session
    }

    override fun clearSession() {
        prefs.edit().clear().apply()
        _sessionFlow.value = null
    }

    override fun getActiveCircleId(): String? {
        return prefs.getString(SessionKeys.KEY_ACTIVE_CIRCLE_ID, null)
    }

    override fun setActiveCircleId(fundId: String) {
        prefs.edit().putString(SessionKeys.KEY_ACTIVE_CIRCLE_ID, fundId).apply()
        val current = _sessionFlow.value
        if (current != null) {
            _sessionFlow.value = current.copy(activeCircleId = fundId)
        }
    }

    private fun loadSessionFromPrefs(): UserSession? {
        val uid = prefs.getString(SessionKeys.KEY_USER_UID, null) ?: return null
        val email = prefs.getString(SessionKeys.KEY_USER_EMAIL, null) ?: return null
        val token = prefs.getString(SessionKeys.KEY_AUTH_TOKEN, null) ?: ""
        val displayName = prefs.getString(SessionKeys.KEY_DISPLAY_NAME, "") ?: ""
        val photoUrl = prefs.getString(SessionKeys.KEY_PHOTO_URL, null)
        val activeCircleId = prefs.getString(SessionKeys.KEY_ACTIVE_CIRCLE_ID, null)

        return UserSession(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            idToken = token,
            activeCircleId = activeCircleId
        )
    }
}
