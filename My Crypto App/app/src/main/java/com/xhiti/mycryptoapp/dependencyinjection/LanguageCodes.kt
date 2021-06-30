package com.baruckis.kriptofolio.dependencyinjection

import java.util.*

object LanguageCodes {
    const val ENGLISH   = "EN"
    const val ITALIAN   = "IT"
    const val GERMAN    = "DE"
    const val RUSSIAN   = "RU"
    const val ALBANIAN  = "SQ"
}

enum class Language(val locale: Locale) {
    English(Locale(LanguageCodes.ENGLISH)),
    Italian(Locale(LanguageCodes.ITALIAN)),
    German(Locale(LanguageCodes.GERMAN)),
    Russian(Locale(LanguageCodes.RUSSIAN)),
    Albanian(Locale(LanguageCodes.ALBANIAN));

    companion object {
        val DEFAULT = English

        fun fromLocale(locale: Locale): Language = values().
                firstOrNull { it.locale.language == locale.language } ?: DEFAULT
    }
}