package com.fundcircle.app.data.datasource.local

import com.fundcircle.app.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Local data source contract for managing encrypted user session and circle preferences.
 */
interface SessionLocalDataSource {
    fun getUserSession(): UserSession?
    fun observeUserSession(): Flow<UserSession?>
    fun saveUserSession(session: UserSession)
    fun clearSession()
    fun getActiveCircleId(): String?
    fun setActiveCircleId(fundId: String)
}
