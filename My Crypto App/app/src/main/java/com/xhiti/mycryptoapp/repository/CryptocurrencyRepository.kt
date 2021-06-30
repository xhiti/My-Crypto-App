package com.baruckis.kriptofolio.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.api.*
import com.baruckis.kriptofolio.db.Cryptocurrency
import com.baruckis.kriptofolio.db.CryptocurrencyDao
import com.baruckis.kriptofolio.db.MyCryptocurrency
import com.baruckis.kriptofolio.db.MyCryptocurrencyDao
import com.baruckis.kriptofolio.utilities.*
import com.baruckis.kriptofolio.utilities.localization.StringsLocalization
import com.baruckis.kriptofolio.vo.Resource
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Singleton
class CryptocurrencyRepository @Inject constructor(
        private val context: Context,
        private val appExecutors: AppExecutors,
        private val myCryptocurrencyDao: MyCryptocurrencyDao,
        private val cryptocurrencyDao: CryptocurrencyDao,
        private val api: ApiService,
        private val sharedPreferences: SharedPreferences,
        private val stringsLocalization: StringsLocalization
) {

    var selectedFiatCurrencyCode: String = getCurrentFiatCurrencyCode()

    fun getMyCryptocurrencyLiveDataResourceList(fiatCurrencyCode: String, shouldFetch: Boolean = false, myCryptocurrenciesIds: String? = null, callDelay: Long = 0): LiveData<Resource<List<MyCryptocurrency>>> {
        return object : NetworkBoundResource<List<MyCryptocurrency>, CoinMarketCap<HashMap<String, CryptocurrencyLatest>>>(appExecutors) {

            override fun saveCallResult(item: CoinMarketCap<HashMap<String, CryptocurrencyLatest>>) {

                val list: MutableList<CryptocurrencyLatest> = ArrayList()

                if (!item.data.isNullOrEmpty()) {
                    for ((_, value) in item.data) {
                        list.add(value)
                    }
                }

                myCryptocurrencyDao.upsert(getMyCryptocurrencyListFromResponse(fiatCurrencyCode, list, item.status?.timestamp))
            }

            override fun shouldFetch(data: List<MyCryptocurrency>?): Boolean {
                return shouldFetch
            }

            override fun fetchDelayMillis(): Long {
                return callDelay
            }

            override fun loadFromDb(): LiveData<List<MyCryptocurrency>> {
                return myCryptocurrencyDao.getMyCryptocurrencyLiveDataList()
            }

            override fun createCall(): LiveData<ApiResponse<CoinMarketCap<HashMap<String, CryptocurrencyLatest>>>> {
                return if (!myCryptocurrenciesIds.isNullOrEmpty())
                    api.getCryptocurrenciesById(fiatCurrencyCode, myCryptocurrenciesIds) else
                    MediatorLiveData<ApiResponse<CoinMarketCap<HashMap<String, CryptocurrencyLatest>>>>().apply { value = ApiEmptyResponse() }
            }

        }.asLiveData()
    }

    fun getMyCryptocurrencyLiveDataList(): LiveData<List<MyCryptocurrency>> {
        return myCryptocurrencyDao.getMyCryptocurrencyLiveDataList()
    }

    fun getMyCryptocurrencyList(): List<MyCryptocurrency>? {
        return myCryptocurrencyDao.getMyCryptocurrencyList()
    }

    fun getMyCryptocurrencyIds(): String? {
        return myCryptocurrencyDao.getMyCryptocurrencyIds()
    }

    fun getAllCryptocurrencyLiveDataResourceList(fiatCurrencyCode: String, shouldFetch: Boolean = false, callDelay: Long = 0): LiveData<Resource<List<Cryptocurrency>>> {
        return object : NetworkBoundResource<List<Cryptocurrency>, CoinMarketCap<List<CryptocurrencyLatest>>>(appExecutors) {

            override fun saveCallResult(item: CoinMarketCap<List<CryptocurrencyLatest>>) {
                val list = getCryptocurrencyListFromResponse(fiatCurrencyCode, item.data, item.status?.timestamp)
                cryptocurrencyDao.reloadCryptocurrencyList(list)
                myCryptocurrencyDao.reloadMyCryptocurrencyList(list)
            }

            override fun shouldFetch(data: List<Cryptocurrency>?): Boolean {
                return data == null || shouldFetch
            }

            override fun fetchDelayMillis(): Long {
                return callDelay
            }

            override fun loadFromDb(): LiveData<List<Cryptocurrency>> {
                return Transformations.switchMap(cryptocurrencyDao.getAllCryptocurrencyLiveDataList()) { data ->
                    if (data.isEmpty()) {
                        AbsentLiveData.create()
                    } else {
                        cryptocurrencyDao.getAllCryptocurrencyLiveDataList()
                    }
                }
            }

            override fun createCall(): LiveData<ApiResponse<CoinMarketCap<List<CryptocurrencyLatest>>>> = api.getAllCryptocurrencies(fiatCurrencyCode)
        }.asLiveData()
    }

    fun getCryptocurrencyLiveDataListBySearch(searchText: String): LiveData<List<Cryptocurrency>> {
        return cryptocurrencyDao.getCryptocurrencyLiveDataListBySearch(searchText)
    }

    fun getSpecificCryptocurrencyLiveData(specificCryptoCode: String): LiveData<Cryptocurrency> {
        return cryptocurrencyDao.getSpecificCryptocurrencyLiveDataByCryptoCode(specificCryptoCode)
    }

    fun upsertMyCryptocurrency(myCryptocurrency: MyCryptocurrency) {
        myCryptocurrencyDao.upsert(listOf(myCryptocurrency), true)
    }

    fun insertMyCryptocurrencyList(myCryptocurrencyList: List<MyCryptocurrency>): List<Long> {
        return myCryptocurrencyDao.insertCryptocurrencyList(myCryptocurrencyList)
    }

    fun deleteMyCryptocurrencyList(myCryptocurrencyList: List<MyCryptocurrency>) {
        myCryptocurrencyDao.deleteCryptocurrencyList(myCryptocurrencyList)
    }

    fun getCurrentDateFormat(): String {
        return sharedPreferences.getString(context.resources.getString(R.string.pref_date_format_key),
                stringsLocalization.getString(R.string.pref_default_date_format_value))
                ?: stringsLocalization.getString(R.string.pref_default_date_format_value)
    }

    fun getCurrentDateFormatLiveData(): LiveData<String> {
        return sharedPreferences.stringLiveData(context.resources.getString(R.string.pref_date_format_key),
                stringsLocalization.getString(R.string.pref_default_date_format_value))
    }

    fun getCurrentTimeFormat(): TimeFormat {
        return if (sharedPreferences.getBoolean(context.resources.getString(R.string.pref_24h_switch_key), true))
            TimeFormat.Hours24() else TimeFormat.Hours12()
    }

    fun getCurrentTimeFormatLiveData(): LiveData<TimeFormat> {
        return Transformations.switchMap(sharedPreferences.
                booleanLiveData(context.resources.getString(R.string.pref_24h_switch_key), true)) {
            MutableLiveData<TimeFormat>().apply {
                value = if (it) TimeFormat.Hours24() else TimeFormat.Hours12()
            }
        }
    }

    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(context.resources.getString(R.string.pref_language_key),
                stringsLocalization.getString(R.string.pref_default_language_value))
                ?: stringsLocalization.getString(R.string.pref_default_language_value)
    }

    fun setNewCurrentFiatCurrencyCode(value: String) {
        selectedFiatCurrencyCode = value
        sharedPreferences.edit().putString(context.resources.getString(R.string.pref_fiat_currency_key),
                value).apply()
    }

    fun getCurrentFiatCurrencyCode(): String {
        return sharedPreferences.getString(context.resources.getString(R.string.pref_fiat_currency_key),
                stringsLocalization.getString(R.string.pref_default_fiat_currency_value))
                ?: stringsLocalization.getString(R.string.pref_default_fiat_currency_value)
    }

    fun getCurrentFiatCurrencyCodeLiveData(): LiveData<String> {
        return sharedPreferences.stringLiveData(context.resources.getString(R.string.pref_fiat_currency_key),
                stringsLocalization.getString(R.string.pref_default_fiat_currency_value))
    }

    fun getCurrentFiatCurrencySign(fiatCurrencyCode: String): String {

        val fiatCurrencySymbols: HashMap<String, String> = HashMap()
        val keys = context.resources.getStringArray(R.array.fiat_currency_code_array)
        val values = context.resources.getStringArray(R.array.fiat_currency_sign_array)

        for (i in 0 until Math.min(keys.size, values.size)) {
            fiatCurrencySymbols.put(keys[i], values[i])
        }

        return fiatCurrencySymbols.asSequence().filter { it.key == fiatCurrencyCode }.first().value
    }

    fun getCurrentFiatCurrencySignLiveData(): LiveData<String> {
        return Transformations.switchMap(getCurrentFiatCurrencyCodeLiveData()) { data ->
            MutableLiveData<String>().takeIf { data != null }?.apply {
                value = getCurrentFiatCurrencySign(data) }
        }
    }

    private fun getMyCryptocurrencyListFromResponse(fiatCurrencyCode: String,
                                                    responseList: List<CryptocurrencyLatest>?,
                                                    timestamp: Date?): ArrayList<MyCryptocurrency> {

        val myCryptocurrencyList: MutableList<MyCryptocurrency> = ArrayList()

        responseList?.forEach {
            val cryptocurrency = Cryptocurrency(it.id, it.name, it.cmcRank.toShort(),
                    it.symbol, fiatCurrencyCode, it.quote.currency.price,
                    it.quote.currency.percentChange1h,
                    it.quote.currency.percentChange7d, it.quote.currency.percentChange24h, timestamp)
            val myCryptocurrency = MyCryptocurrency(it.id, cryptocurrency)
            myCryptocurrencyList.add(myCryptocurrency)
        }

        return myCryptocurrencyList as ArrayList<MyCryptocurrency>
    }

    private fun getCryptocurrencyListFromResponse(fiatCurrencyCode: String,
                                                  responseList: List<CryptocurrencyLatest>?,
                                                  timestamp: Date?): ArrayList<Cryptocurrency> {

        val cryptocurrencyList: MutableList<Cryptocurrency> = ArrayList()

        responseList?.forEach {
            val cryptocurrency = Cryptocurrency(it.id, it.name, it.cmcRank.toShort(),
                    it.symbol, fiatCurrencyCode, it.quote.currency.price,
                    it.quote.currency.percentChange1h,
                    it.quote.currency.percentChange7d, it.quote.currency.percentChange24h, timestamp)
            cryptocurrencyList.add(cryptocurrency)
        }

        return cryptocurrencyList as ArrayList<Cryptocurrency>
    }

}