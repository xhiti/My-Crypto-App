package com.baruckis.kriptofolio.utilities.localization

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION_CODES.N
import androidx.preference.PreferenceManager
import com.baruckis.kriptofolio.R
import java.util.*

object LocalizationManager {

    fun setLocale(context: Context): Context {
        return updateResources(context, getLanguage(context))
    }

    fun getLocale(res: Resources): Locale {
        val config = res.configuration
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= N) config.locales.get(0) else config.locale
    }


    private fun getLanguage(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_language_key),
                context.getString(R.string.pref_default_language_value))
                ?: context.getString(R.string.pref_default_language_value)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}