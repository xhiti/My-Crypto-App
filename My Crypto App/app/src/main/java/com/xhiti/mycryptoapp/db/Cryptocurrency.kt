package com.baruckis.kriptofolio.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "all_cryptocurrencies")
@Parcelize
data class Cryptocurrency(@PrimaryKey
                          @ColumnInfo(name = "id")
                          val id: Int,
                          @ColumnInfo(name = "name")
                          var name: String,
                          @ColumnInfo(name = "rank")
                          var rank: Short,
                          @ColumnInfo(name = "symbol")
                          var symbol: String,
                          @ColumnInfo(name = "currency_fiat")
                          var currencyFiat: String,
                          @ColumnInfo(name = "price_fiat")
                          var priceFiat: Double,
                          @ColumnInfo(name = "price_percent_change_1h")
                          var pricePercentChange1h: Double,
                          @ColumnInfo(name = "price_percent_change_7d")
                          var pricePercentChange7d: Double,
                          @ColumnInfo(name = "price_percent_change_24h")
                          var pricePercentChange24h: Double,
                          @ColumnInfo(name = "last_fetched_date")
                          var lastFetchedDate: Date?) : Parcelable