package com.fundcircle.app.core.theme

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemePreferenceManagerTest {

    private fun createFakePrefs(initial: Map<String, Any?> = emptyMap()): SharedPreferences {
        val storage = initial.toMutableMap()
        return object : SharedPreferences {
            private val editor = object : SharedPreferences.Editor {
                private val pending = mutableMapOf<String, Any?>()
                private var clearRequested = false

                override fun putString(key: String?, value: String?): SharedPreferences.Editor = this
                override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
                override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this
                override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
                override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
                override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                    if (key != null) pending[key] = value
                    return this
                }

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

            override fun getAll(): MutableMap<String, *> = storage
            override fun getString(key: String?, defValue: String?): String? = defValue
            override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
            override fun getInt(key: String?, defValue: Int): Int = defValue
            override fun getLong(key: String?, defValue: Long): Long = defValue
            override fun getFloat(key: String?, defValue: Float): Float = defValue
            override fun getBoolean(key: String?, defValue: Boolean): Boolean =
                storage[key] as? Boolean ?: defValue

            override fun contains(key: String?): Boolean = storage.containsKey(key)
            override fun edit(): SharedPreferences.Editor = editor
            override fun registerOnSharedPreferenceChangeListener(
                listener: SharedPreferences.OnSharedPreferenceChangeListener?
            ) = Unit

            override fun unregisterOnSharedPreferenceChangeListener(
                listener: SharedPreferences.OnSharedPreferenceChangeListener?
            ) = Unit
        }
    }

    @Test
    fun `initial override is null when no preference was persisted`() {
        val manager = ThemePreferenceManager(createFakePrefs())
        assertThat(manager.darkThemeOverride.value).isNull()
    }

    @Test
    fun `setDarkTheme persists boolean and emits across new instances`() {
        val prefs = createFakePrefs()
        val manager1 = ThemePreferenceManager(prefs)

        manager1.setDarkTheme(true)
        assertThat(manager1.darkThemeOverride.value).isTrue()

        val reloadedManager = ThemePreferenceManager(prefs)
        assertThat(reloadedManager.darkThemeOverride.value).isTrue()

        reloadedManager.setDarkTheme(false)
        assertThat(reloadedManager.darkThemeOverride.value).isFalse()

        val reloadedLightManager = ThemePreferenceManager(prefs)
        assertThat(reloadedLightManager.darkThemeOverride.value).isFalse()
    }
}
