package com.baruckis.kriptofolio.utilities

import android.util.Log
import com.baruckis.kriptofolio.BuildConfig

fun logConsoleVerbose(message: String) {
    if (BuildConfig.DEBUG) {
        Log.v(LOG_TAG, message)
    }
}

fun logConsoleWarn(message: String) {
    if (BuildConfig.DEBUG) {
        Log.w(LOG_TAG, message)
    }
}

fun logConsoleError(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(LOG_TAG, message)
    }
}