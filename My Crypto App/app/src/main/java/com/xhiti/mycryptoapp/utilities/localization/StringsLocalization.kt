package com.baruckis.kriptofolio.utilities.localization

import android.content.res.Resources
import androidx.annotation.StringRes
import com.baruckis.kriptofolio.dependencyinjection.Language
import com.baruckis.kriptofolio.utilities.logConsoleError
import com.baruckis.kriptofolio.utilities.logConsoleWarn
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringsLocalization @Inject constructor(
        private val localization: Localization,
        private val resMap: Map<Language, @JvmSuppressWildcards Resources>
) {
    fun setLanguage(languageCode: String) {
        val newLanguage = try {
            Language.fromLocale(Locale(languageCode))
        } catch (ex: Exception) {
            logConsoleError("Language for code $languageCode is not found.")
            null
        }
        newLanguage?.let { localization.currentLanguage = it }
    }

    fun getString(@StringRes stringId: Int): String = resMap.getOrElse(localization.currentLanguage, this::getFallbackResources).getString(stringId)

    fun getString(@StringRes stringId: Int, vararg formatArgs: Any): String =
            resMap.getOrElse(localization.currentLanguage, this::getFallbackResources).getString(stringId, *formatArgs)

    private fun getFallbackResources(): Resources {
        val defaultLanguage =
                if (Language.DEFAULT in resMap) Language.DEFAULT else resMap.keys.firstOrNull()

        if (defaultLanguage != null) {
            logConsoleWarn("Current language resources not found. Fallback to: $defaultLanguage")
            localization.currentLanguage = defaultLanguage
            return resMap.getValue(defaultLanguage)
        } else {
            throw ResourcesNotFoundException("String resources not found.")
        }
    }
}

class ResourcesNotFoundException(message: String) : RuntimeException(message)