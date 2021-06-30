package com.baruckis.kriptofolio.utilities

fun getAmountFiatCounted(amount: Double?, priceFiat: Double): Double? =
        amount?.let { it -> it * priceFiat }

fun getAmountFiatChange24hCounted(amountFiat: Double?, pricePercentChange24h: Double): Double? =
        amountFiat?.let { it -> it * (pricePercentChange24h / 100) }