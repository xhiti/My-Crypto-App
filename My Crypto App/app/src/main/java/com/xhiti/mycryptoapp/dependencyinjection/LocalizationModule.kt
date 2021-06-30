package com.baruckis.kriptofolio.dependencyinjection

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import com.baruckis.kriptofolio.utilities.localization.Localization
import com.baruckis.kriptofolio.utilities.localization.LocalizationLanguage
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import java.util.*
import javax.inject.Singleton

@Module
class LocalizationModule {

    @Provides
    @IntoMap
    @LanguageKey(Language.English)
    fun provideEnglishResources(context: Context): Resources =
            getLocalizedResources(context, Language.English.locale)

    @Provides
    @IntoMap
    @LanguageKey(Language.Italian)
    fun provideHebrewResources(context: Context): Resources =
            getLocalizedResources(context, Language.Italian.locale)

    @Provides
    @IntoMap
    @LanguageKey(Language.German)
    fun provideLithuanianResources(context: Context): Resources =
            getLocalizedResources(context, Language.German.locale)

    @Provides
    @IntoMap
    @LanguageKey(Language.Russian)
    fun provideSwahiliResources(context: Context): Resources =
            getLocalizedResources(context, Language.Russian.locale)

    @Provides
    @IntoMap
    @LanguageKey(Language.Albanian)
    fun provideAlbanianResources(context: Context): Resources =
            getLocalizedResources(context, Language.Albanian.locale)


    @Provides
    @Singleton
    fun provideLocalization(context: Context, sharedPreferences: SharedPreferences): Localization =
            LocalizationLanguage(context, sharedPreferences)


    private fun getLocalizedResources(context: Context, locale: Locale): Resources {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        val localizedContext = context.createConfigurationContext(configuration)
        return localizedContext.resources
    }

}