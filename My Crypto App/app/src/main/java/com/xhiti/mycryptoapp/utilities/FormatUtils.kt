package com.baruckis.kriptofolio.utilities

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.baruckis.kriptofolio.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

enum class ValueType(val pattern: String) {
    Crypto(CRYPTO_FORMAT_PATTERN),
    Fiat(FIAT_FORMAT_PATTERN),
    Percent(PERCENT_FORMAT_PATTERN)
}

sealed class SpannableValueColorStyle {
    object Foreground : SpannableValueColorStyle()
    object Background : SpannableValueColorStyle()
}

fun roundValue(number: Double?, type: ValueType): String {
    val df = DecimalFormat(type.pattern)
    df.roundingMode = RoundingMode.DOWN
    return df.format(number)
}

fun getSpannableValueStyled(context: Context, value: Double?, style: SpannableValueColorStyle, type: ValueType, left: String = "", right: String = "", textIfNaN: String? = ""): SpannableString {
    val valueSpannable: SpannableString
    var vl = value
    var valueColor = ContextCompat.getColor(context, R.color.colorForMainListItemText)

    fun getColorSpan(color: Int): CharacterStyle {
        return when (style) {
            is SpannableValueColorStyle.Foreground -> ForegroundColorSpan(color)
            is SpannableValueColorStyle.Background -> BackgroundColorSpan(color)
        }
    }

    var leftMod = left

    if (vl == null) {
        vl = 0.0
    }

    when {
        vl > 0 -> {
            valueColor = ContextCompat.getColor(context, R.color.colorForValueChangePositive)
            leftMod = leftMod.plus("+")
        }
        vl < 0 -> {
            valueColor = ContextCompat.getColor(context, R.color.colorForValueChangeNegative)
        }
    }

    valueSpannable = if (vl.isNaN()) SpannableString("$leftMod$textIfNaN$right") else
        SpannableString("$leftMod${roundValue(vl, type)}$right")

    valueSpannable.setSpan(getColorSpan(valueColor), 0, valueSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    return valueSpannable
}

fun getTextFirstChars(text: String?, charLimit: Int): String {
    return if (text.isNullOrEmpty()) ""
    else text.substring(0, Math.min(text.length, charLimit))
}


sealed class TimeFormat(val pattern: String) {
    class Hours12 : TimeFormat(TIME_12h_FORMAT_PATTERN)
    class Hours24 : TimeFormat(TIME_24h_FORMAT_PATTERN)
}

fun formatDate(timeStamp: Date?, dateFormatPattern: String?, timeFormatPattern: TimeFormat? = null,
               textAM: String? = null, textPM: String? = null): String {
    return if (timeStamp == null || dateFormatPattern == null) "" else {

        var pattern = dateFormatPattern
        var addOn = ""

        when (timeFormatPattern) {
            is TimeFormat.Hours12 -> {
                val calendar: Calendar = Calendar.getInstance()
                calendar.time = timeStamp
                when (calendar.get(Calendar.AM_PM)) {
                    Calendar.AM -> addOn = textAM ?: ""
                    Calendar.PM -> addOn = textPM ?: ""
                }
            }
        }

        pattern += if (timeFormatPattern != null) " " + timeFormatPattern.pattern else ""

        val sdf = SimpleDateFormat(pattern, Locale.getDefault())

        var value = sdf.format(timeStamp)
        if (addOn.isNotEmpty()) value += " $addOn"

        return value
    }
}
