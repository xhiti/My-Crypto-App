package com.baruckis.kriptofolio.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyCryptocurrency::class, Cryptocurrency::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun myCryptocurrencyDao(): MyCryptocurrencyDao

    abstract fun cryptocurrencyDao(): CryptocurrencyDao
}