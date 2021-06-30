package com.baruckis.kriptofolio.dependencyinjection

import com.baruckis.kriptofolio.ui.addsearchlist.AddSearchActivity
import com.baruckis.kriptofolio.ui.mainlist.MainActivity
import com.baruckis.kriptofolio.ui.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(modules = [MainListFragmetBuildersModule::class]) // Where to apply the injection.
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeAddSearchActivity(): AddSearchActivity

    @ContributesAndroidInjector(modules = [SettingsFragmetsBuildersModule::class]) // Where to apply the injection.
    abstract fun contributeSettingsActivity(): SettingsActivity
}