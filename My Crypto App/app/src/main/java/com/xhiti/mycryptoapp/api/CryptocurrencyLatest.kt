package com.baruckis.kriptofolio.api

import com.google.gson.annotations.SerializedName

data class CryptocurrencyLatest(
        val id: Int,
        val name: String,
        val symbol: String,
        val slug: String,

        @SerializedName("circulating_supply")
        val circulatingSupply: Double,
        @SerializedName("total_supply")
        val totalSupply: Double,
        @SerializedName("max_supply")
        val maxSupply: Double,
        @SerializedName("date_added")
        val dateAdded: String,
        @SerializedName("num_market_pairs")
        val numMarketPairs: Int,
        @SerializedName("cmc_rank")
        val cmcRank: Int,
        @SerializedName("last_updated")
        val lastUpdated: String,
        val quote: Quote
) {

    data class Quote(
            @SerializedName(value = "USD", alternate = [
                "ALL", "DZD", "ARS", "AMD", "AUD", "AZN", "BHD", "BDT", "BYN", "BMD", "BOB", "BAM",
                "BRL", "BGN", "KHR", "CAD", "CLP", "CNY", "COP", "CRC", "HRK", "CUP", "CZK", "DKK",
                "DOP", "EGP", "EUR", "GEL", "GHS", "GTQ", "HNL", "HKD", "HUF", "ISK", "INR", "IDR",
                "IRR", "IQD", "ILS", "JMD", "JPY", "JOD", "KZT", "KES", "KWD", "KGS", "LBP", "MKD",
                "MYR", "MUR", "MXN", "MDL", "MNT", "MAD", "MMK", "NAD", "NPR", "TWD", "NZD", "NIO",
                "NGN", "NOK", "OMR", "PKR", "PAB", "PEN", "PHP", "PLN", "GBP", "QAR", "RON", "RUB",
                "SAR", "RSD", "SGD", "ZAR", "KRW", "SSP", "VES", "LKR", "SEK", "CHF", "THB", "TTD",
                "TND", "TRY", "UGX", "UAH", "AED", "UYU", "UZS", "VND"
            ])
            val currency: Currency
    ) {

        data class Currency(
                val price: Double,
                @SerializedName("volume_24h")
                val volume24h: Double,
                @SerializedName("percent_change_1h")
                val percentChange1h: Double,
                @SerializedName("percent_change_24h")
                val percentChange24h: Double,
                @SerializedName("percent_change_7d")
                val percentChange7d: Double,
                @SerializedName("market_cap")
                val marketCap: Double,
                @SerializedName("last_updated")
                val lastUpdated: String
        )
    }
}