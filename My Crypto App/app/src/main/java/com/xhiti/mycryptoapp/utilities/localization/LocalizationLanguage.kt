package com.baruckis.kriptofolio.utilities.localization

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.dependencyinjection.Language
import java.util.*

interface Localization {
    var currentLanguage: Language
}

class LocalizationLanguage(
        private val context: Context,
        private val sharedPreferences: SharedPreferences
) : Localization {

    private var currentLanguageCache: Language? = null

    override var currentLanguage: Language
        get() {
            val cachedValue = currentLanguageCache
            return if (cachedValue == null) {
                val storedValue = sharedPreferences.getString(
                        context.getString(R.string.pref_language_key),
                        context.getString(R.string.pref_default_language_value))
                val storedLanguage = if (storedValue == null) null else try {
                    Language.fromLocale(Locale(storedValue))
                } catch (ex: Exception) {
                    null
                }

                val language = storedLanguage ?: getDefaultLanguage()
                currentLanguage = language
                language
            } else cachedValue
        }
        set(value) {
            currentLanguageCache = value
        }

    private fun getDefaultLanguage(): Language {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return Language.fromLocale(locale)
    }

}