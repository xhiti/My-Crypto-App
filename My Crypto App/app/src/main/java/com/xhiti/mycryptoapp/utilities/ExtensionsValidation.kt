package com.baruckis.kriptofolio.utilities

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
    })
}

fun EditText.nonEmpty(onEmpty: (() -> Unit), onNotEmpty: (() -> Unit)) {
    if (this.text.toString().isEmpty()) onEmpty.invoke()
    this.afterTextChanged {
        if (it.isEmpty()) onEmpty.invoke()
        if (it.isNotEmpty()) onNotEmpty.invoke()
    }
}

fun EditText.validate(validator: (String) -> Boolean, message: String):Boolean {
    val isValid = validator(this.text.toString())
    this.error = if (isValid) null else message
    return isValid
}