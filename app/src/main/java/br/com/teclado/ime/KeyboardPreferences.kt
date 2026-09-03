package br.com.teclado.ime

import android.content.Context

class KeyboardPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var hapticEnabled: Boolean
        get() = preferences.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) {
            preferences.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()
        }

    companion object {
        private const val PREFERENCES_NAME = "keyboard_preferences"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
    }
}
