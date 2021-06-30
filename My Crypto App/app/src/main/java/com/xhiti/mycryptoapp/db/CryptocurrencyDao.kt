package com.baruckis.kriptofolio.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class CryptocurrencyDao {

    @Query("SELECT * FROM all_cryptocurrencies ORDER BY rank ASC")
    abstract fun getAllCryptocurrencyLiveDataList(): LiveData<List<Cryptocurrency>>

    @Query("DELETE FROM all_cryptocurrencies")
    abstract fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCryptocurrencyList(itemList: List<Cryptocurrency>): List<Long>

    @Transaction
    open fun reloadCryptocurrencyList(itemList: List<Cryptocurrency>) {
        deleteAll()
        insertCryptocurrencyList(itemList)
    }

    @Query("SELECT * FROM all_cryptocurrencies WHERE name LIKE :searchText OR symbol LIKE :searchText ORDER BY rank ASC")
    abstract fun getCryptocurrencyLiveDataListBySearch(searchText: String): LiveData<List<Cryptocurrency>>

    @Query("SELECT * FROM all_cryptocurrencies WHERE symbol = :specificCryptoCode LIMIT 1")
    abstract fun getSpecificCryptocurrencyLiveDataByCryptoCode(specificCryptoCode: String): LiveData<Cryptocurrency>

}