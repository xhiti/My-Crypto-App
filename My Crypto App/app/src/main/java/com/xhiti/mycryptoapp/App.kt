package com.baruckis.kriptofolio

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.baruckis.kriptofolio.dependencyinjection.AppInjector
import com.baruckis.kriptofolio.utilities.localization.LocalizationManager
import com.baruckis.kriptofolio.utilities.logConsoleVerbose
import com.facebook.stetho.Stetho
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        Stetho.initializeWithDefaults(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocalizationManager.setLocale(base))
        logConsoleVerbose("attachBaseContext " + this@App.toString())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalizationManager.setLocale(this)
        logConsoleVerbose("onConfigurationChanged")
    }

}