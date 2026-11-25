package com.fundcircle.app.core.theme

import android.content.SharedPreferences
import com.fundcircle.app.core.security.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists user-selected theme override (Light / Dark) independently from encrypted auth state.
 * A `null` override means the app follows the OS system theme.
 */
@Singleton
class ThemePreferenceManager @Inject constructor(
    @AppPreferences private val prefs: SharedPreferences
) {
    private val _darkThemeOverride = MutableStateFlow(readPreference())
    val darkThemeOverride: StateFlow<Boolean?> = _darkThemeOverride.asStateFlow()

    /**
     * Persists an explicit dark or light mode override and notifies collectors immediately.
     */
    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME_OVERRIDE, isDark).apply()
        _darkThemeOverride.value = isDark
    }

    private fun readPreference(): Boolean? {
        return if (prefs.contains(KEY_DARK_THEME_OVERRIDE)) {
            prefs.getBoolean(KEY_DARK_THEME_OVERRIDE, false)
        } else {
            null
        }
    }

    companion object {
        const val KEY_DARK_THEME_OVERRIDE = "key_dark_theme_override"
    }
}
