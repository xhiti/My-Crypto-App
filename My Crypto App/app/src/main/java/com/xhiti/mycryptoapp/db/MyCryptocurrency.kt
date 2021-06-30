package com.baruckis.kriptofolio.db

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "my_cryptocurrencies")
@Parcelize
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
data class MyCryptocurrency(
        @PrimaryKey
        @ColumnInfo(name = "my_id")
        val myId: Int,
        @Embedded var cryptoData: Cryptocurrency,
        @ColumnInfo(name = "amount")
        var amount: Double? = null,
        @ColumnInfo(name = "amount_fiat")
        var amountFiat: Double? = null,
        @ColumnInfo(name = "amount_fiat_change_24h")
        var amountFiatChange24h: Double? = null) : Parcelable