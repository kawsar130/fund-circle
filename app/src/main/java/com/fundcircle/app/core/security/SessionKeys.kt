package com.fundcircle.app.core.security

/**
 * Keystore Storage Contract: Key-Value Mapping as defined in API Specification § 2.4.
 */
object SessionKeys {
    const val PREFS_NAME = "fc_encrypted_session_prefs"
    const val KEY_AUTH_TOKEN = "fc_auth_token"
    const val KEY_USER_UID = "fc_user_uid"
    const val KEY_USER_EMAIL = "fc_user_email"
    const val KEY_DISPLAY_NAME = "fc_display_name"
    const val KEY_PHOTO_URL = "fc_photo_url"
    const val KEY_ACTIVE_CIRCLE_ID = "fc_active_circle_id"
}
