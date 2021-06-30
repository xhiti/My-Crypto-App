package com.baruckis.kriptofolio.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.baruckis.kriptofolio.utilities.getAmountFiatChange24hCounted
import com.baruckis.kriptofolio.utilities.getAmountFiatCounted

@Dao
abstract class MyCryptocurrencyDao {

    @Query("SELECT * FROM my_cryptocurrencies WHERE amount IS NOT NULL ORDER BY amount_fiat DESC, rank ASC")
    abstract fun getMyCryptocurrencyLiveDataList(): LiveData<List<MyCryptocurrency>>

    @Query("SELECT GROUP_CONCAT(id) FROM my_cryptocurrencies WHERE amount IS NOT NULL")
    abstract fun getMyCryptocurrencyIds(): String

    @Query("SELECT * FROM my_cryptocurrencies")
    abstract fun getMyCryptocurrencyList(): List<MyCryptocurrency>

    @Query("SELECT * FROM my_cryptocurrencies WHERE id = :id LIMIT 1")
    abstract fun getSpecificCryptocurrencyById(id: Int): MyCryptocurrency

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateCryptocurrency(myCryptocurrency: MyCryptocurrency): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateCryptocurrencyList(myCryptocurrencyList: List<MyCryptocurrency>): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertCryptocurrencyList(itemList: List<MyCryptocurrency>): List<Long>

    @Transaction
    open fun upsert(itemList: List<MyCryptocurrency>, updateAmount: Boolean = false) {
        val insertResult = insertCryptocurrencyList(itemList)
        val updateList = ArrayList<MyCryptocurrency>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(itemList[i])
            }
        }

        if (!updateList.isEmpty()) {
            for (updateItem in updateList) {
                val currentItem = getSpecificCryptocurrencyById(updateItem.myId)
                currentItem.cryptoData.name = updateItem.cryptoData.name
                currentItem.cryptoData.rank = updateItem.cryptoData.rank
                currentItem.cryptoData.symbol = updateItem.cryptoData.symbol
                currentItem.cryptoData.currencyFiat = updateItem.cryptoData.currencyFiat
                currentItem.cryptoData.priceFiat = updateItem.cryptoData.priceFiat
                currentItem.cryptoData.pricePercentChange1h = updateItem.cryptoData.pricePercentChange1h
                currentItem.cryptoData.pricePercentChange7d = updateItem.cryptoData.pricePercentChange7d
                currentItem.cryptoData.pricePercentChange24h = updateItem.cryptoData.pricePercentChange24h
                currentItem.cryptoData.lastFetchedDate = updateItem.cryptoData.lastFetchedDate

                if (updateAmount) {
                    currentItem.amount = updateItem.amount
                }

                if (currentItem.amount != null) {
                    currentItem.amountFiat =
                            getAmountFiatCounted(currentItem.amount, currentItem.cryptoData.priceFiat)
                    currentItem.amountFiatChange24h =
                            getAmountFiatChange24hCounted(currentItem.amountFiat, currentItem.cryptoData.pricePercentChange24h)
                }

                updateCryptocurrency(currentItem)
            }
        }
    }

    @Delete
    abstract fun deleteCryptocurrencyList(itemList: List<MyCryptocurrency>)

    @Transaction
    open fun reloadMyCryptocurrencyList(cryptocurrencyList: List<Cryptocurrency>) {
        val myCryptocurrencyList = getMyCryptocurrencyList()
        myCryptocurrencyList.forEach { myCryptocurrency ->
            val cryptocurrency = cryptocurrencyList.find { cryptocurrency -> cryptocurrency.id == myCryptocurrency.myId }
            cryptocurrency?.let {
                myCryptocurrency.cryptoData = it
                myCryptocurrency.amountFiat =
                        getAmountFiatCounted(myCryptocurrency.amount, myCryptocurrency.cryptoData.priceFiat)
                myCryptocurrency.amountFiatChange24h =
                        getAmountFiatChange24hCounted(myCryptocurrency.amountFiat, myCryptocurrency.cryptoData.pricePercentChange24h)
            }
        }

        if (!myCryptocurrencyList.isEmpty()) updateCryptocurrencyList(myCryptocurrencyList)
    }

}