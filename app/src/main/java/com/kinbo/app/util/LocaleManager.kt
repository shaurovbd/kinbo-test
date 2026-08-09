package com.kinbo.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Manages per-app language using the AndroidX AppCompat per-app language API.
 * Persists choice in SharedPreferences and applies it app-wide.
 */
object LocaleManager {

    private const val PREFS = "kinbo_prefs"
    private const val KEY_LANG = "app_language"

    /** Language codes supported by the app. */
    val supportedLanguages = listOf("en" to "English", "bn" to "বাংলা")

    /** Returns the saved language code, or "en" if none saved. */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    /** Applies the saved language on app startup. */
    fun applySavedLanguage(context: Context) {
        val code = getCurrentLanguage(context)
        AppCompatDelegate.setApplicationLocales(
            if (code == "en") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(code)
        )
    }

    /**
     * Switches the app language and recreates the activity to apply it.
     * "en" uses the system default (empty locale list).
     */
    fun setLanguage(context: Context, code: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, code).apply()
        AppCompatDelegate.setApplicationLocales(
            if (code == "en") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(code)
        )
    }
}
