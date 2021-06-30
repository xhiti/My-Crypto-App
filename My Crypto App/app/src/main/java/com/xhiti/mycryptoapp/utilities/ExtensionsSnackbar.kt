package com.baruckis.kriptofolio.utilities

import android.view.View
import androidx.annotation.StringRes
import com.baruckis.kriptofolio.R
import com.google.android.material.snackbar.Snackbar

inline fun View.showSnackbar(@StringRes messageRes: Int,
                             @Snackbar.Duration length: Int = Snackbar.LENGTH_INDEFINITE,
                             f: Snackbar.() -> Unit): Snackbar {
    return showSnackbar(resources.getString(messageRes), length, f)
}

inline fun View.showSnackbar(message: String, length: Int = Snackbar.LENGTH_INDEFINITE,
                             f: Snackbar.() -> Unit): Snackbar {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
    return snack
}

fun Snackbar.onActionButtonClick(@StringRes textRes: Int = R.string.retry, color: Int? = null,
                                 listener: (View) -> Unit) {
    onActionButtonClick(view.resources.getString(textRes), color, listener)
}

fun Snackbar.onActionButtonClick(text: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(text, listener)
    color?.let { setActionTextColor(color) }
}

fun Snackbar.onDismissedAction(callback: () -> Unit) {
    addCallback(object : Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                callback()
            }
            removeCallback(this)
        }
    })
}

fun Snackbar.onDismissedAnyOfEvents(events: List<Int>,callback: () -> Unit) {
    addCallback(object : Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            events.forEach {
                if (event == it) {
                    callback()
                    return@forEach
                }
            }
            removeCallback(this)
        }
    })
}