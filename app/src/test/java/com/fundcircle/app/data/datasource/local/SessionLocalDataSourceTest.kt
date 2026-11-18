package com.fundcircle.app.data.datasource.local

import android.content.SharedPreferences
import com.fundcircle.app.core.security.SessionKeys
import com.fundcircle.app.domain.model.UserSession
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class SessionLocalDataSourceTest {

    private val storage = mutableMapOf<String, Any?>()
    private val fakeEditor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearRequested = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) pending[key] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearRequested = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearRequested) {
                storage.clear()
                clearRequested = false
            }
            pending.forEach { (k, v) ->
                if (v == null) storage.remove(k) else storage[k] = v
            }
            pending.clear()
        }
    }

    private val fakePrefs = object : SharedPreferences {
        override fun getAll(): MutableMap<String, *> = storage
        override fun getString(key: String?, defValue: String?): String? =
            storage[key] as? String ?: defValue

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = defValue
        override fun getLong(key: String?, defValue: Long): Long = defValue
        override fun getFloat(key: String?, defValue: Float): Float = defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
        override fun contains(key: String?): Boolean = storage.containsKey(key)
        override fun edit(): SharedPreferences.Editor = fakeEditor
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    private lateinit var dataSource: SessionLocalDataSourceImpl

    @Before
    fun setUp() {
        storage.clear()
        dataSource = SessionLocalDataSourceImpl(fakePrefs)
    }

    @Test
    fun initialSession_whenPrefsEmpty_isNull() {
        assertThat(dataSource.getUserSession()).isNull()
    }

    @Test
    fun saveUserSession_persistsAllSessionKeysAndUpdatesFlow() {
        val session = UserSession(
            uid = "uid-123",
            email = "karim@fundcircle.app",
            displayName = "Karim Khan",
            photoUrl = "https://example.com/karim.png",
            idToken = "token-abc",
            activeCircleId = "fund-001"
        )

        dataSource.saveUserSession(session)

        assertThat(dataSource.getUserSession()).isEqualTo(session)
        assertThat(storage[SessionKeys.KEY_USER_UID]).isEqualTo("uid-123")
        assertThat(storage[SessionKeys.KEY_USER_EMAIL]).isEqualTo("karim@fundcircle.app")
        assertThat(storage[SessionKeys.KEY_DISPLAY_NAME]).isEqualTo("Karim Khan")
        assertThat(storage[SessionKeys.KEY_PHOTO_URL]).isEqualTo("https://example.com/karim.png")
        assertThat(storage[SessionKeys.KEY_AUTH_TOKEN]).isEqualTo("token-abc")
        assertThat(storage[SessionKeys.KEY_ACTIVE_CIRCLE_ID]).isEqualTo("fund-001")
    }

    @Test
    fun saveUserSession_withNullPhotoUrl_removesPhotoUrlKeyFromStorage() {
        storage[SessionKeys.KEY_PHOTO_URL] = "https://example.com/old.png"

        val sessionWithoutPhoto = UserSession(
            uid = "uid-123",
            email = "karim@fundcircle.app",
            displayName = "Karim Khan",
            photoUrl = null,
            idToken = "token-abc",
            activeCircleId = null
        )

        dataSource.saveUserSession(sessionWithoutPhoto)

        assertThat(dataSource.getUserSession()?.photoUrl).isNull()
        assertThat(storage.containsKey(SessionKeys.KEY_PHOTO_URL)).isFalse()
    }

    @Test
    fun clearSession_clearsAllStorageAndEmitsNull() {
        val session = UserSession(
            uid = "uid-123",
            email = "karim@fundcircle.app",
            displayName = "Karim Khan",
            photoUrl = null,
            idToken = "token-abc",
            activeCircleId = null
        )
        dataSource.saveUserSession(session)
        assertThat(dataSource.getUserSession()).isNotNull()

        dataSource.clearSession()

        assertThat(dataSource.getUserSession()).isNull()
        assertThat(storage).isEmpty()
    }

    @Test
    fun setActiveCircleId_persistsAndUpdatesActiveSession() {
        val session = UserSession(
            uid = "uid-123",
            email = "karim@fundcircle.app",
            displayName = "Karim Khan",
            photoUrl = null,
            idToken = "token-abc",
            activeCircleId = null
        )
        dataSource.saveUserSession(session)

        dataSource.setActiveCircleId("fund-active-999")

        assertThat(dataSource.getActiveCircleId()).isEqualTo("fund-active-999")
        assertThat(dataSource.getUserSession()?.activeCircleId).isEqualTo("fund-active-999")
    }
}
