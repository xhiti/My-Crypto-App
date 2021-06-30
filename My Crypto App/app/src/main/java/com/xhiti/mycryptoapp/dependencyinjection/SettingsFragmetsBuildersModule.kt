package com.baruckis.kriptofolio.dependencyinjection

import com.baruckis.kriptofolio.ui.settings.thirdpartysoft.LibrariesLicensesListFragment
import com.baruckis.kriptofolio.ui.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmetsBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector()
    abstract fun contributeLibrariesLicensesListFragment(): LibrariesLicensesListFragment
}