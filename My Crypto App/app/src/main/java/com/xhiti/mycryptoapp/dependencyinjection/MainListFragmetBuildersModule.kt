package com.baruckis.kriptofolio.dependencyinjection

import com.baruckis.kriptofolio.ui.mainlist.MainListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainListFragmetBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeMainListFragment(): MainListFragment
}