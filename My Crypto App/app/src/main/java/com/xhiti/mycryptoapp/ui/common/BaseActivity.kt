package com.baruckis.kriptofolio.ui.common

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.baruckis.kriptofolio.BuildConfig
import com.baruckis.kriptofolio.utilities.localization.LocalizationManager
import com.baruckis.kriptofolio.utilities.logConsoleVerbose
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalizationManager.setLocale(newBase))
        logConsoleVerbose("attachBaseContext " + this@BaseActivity.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logConsoleVerbose("onCreate " + this@BaseActivity.toString())
        resetActivityTitle(this)
        logLocalizationInfo()
    }

    private fun resetActivityTitle(a: Activity) {
        try {
            val info = a.packageManager.
                    getActivityInfo(a.componentName, PackageManager.GET_META_DATA)
            if (info.labelRes != 0) {
                a.setTitle(info.labelRes)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun logLocalizationInfo() {
        if (BuildConfig.DEBUG) {
            val topLevelRes = getTopLevelResources(this)
            val appRes = application.resources
            val actRes = resources
            val defLanguage = Locale.getDefault().language

            logConsoleVerbose("Language top level: " + LocalizationManager.getLocale(topLevelRes).language)
            logConsoleVerbose("Language application: " + LocalizationManager.getLocale(appRes).language)
            logConsoleVerbose("Language activity: " + LocalizationManager.getLocale(actRes).language)
            logConsoleVerbose("Language default: $defLanguage")
        }
    }

    private fun getTopLevelResources(a: Activity): Resources {
        try {
            return a.packageManager.getResourcesForApplication(a.applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
    }
}