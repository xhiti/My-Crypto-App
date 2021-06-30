package com.baruckis.kriptofolio.ui.mainlist

import android.content.Context
import android.text.SpannableString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.db.Cryptocurrency
import com.baruckis.kriptofolio.db.MyCryptocurrency
import com.baruckis.kriptofolio.repository.CryptocurrencyRepository
import com.baruckis.kriptofolio.ui.common.BaseViewModel
import com.baruckis.kriptofolio.utilities.*
import com.baruckis.kriptofolio.utilities.localization.StringsLocalization
import com.baruckis.kriptofolio.vo.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val context: Context,
        private val cryptocurrencyRepository: CryptocurrencyRepository,
        private val stringsLocalization: StringsLocalization
        ) : BaseViewModel() {

    private var currentCryptoCurrencyCode: String
    private var currentCryptoCurrencySign: String
    var liveDataCurrentDateFormat: LiveData<String>
    var liveDataCurrentFiatCurrencyCode: LiveData<String>
    val liveDataCurrentFiatCurrencySign: LiveData<String>
    private val liveDataCurrentMyCryptocurrency: LiveData<Cryptocurrency>
    private val liveDataTotalHoldingsValueFiat: LiveData<Double>
    private val liveDataTotalHoldingsValueCrypto: LiveData<Double>
    private val liveDataTotalHoldingsValueFiat24h: LiveData<Double>?
    val mediatorLiveDataMyCryptocurrencyResourceList = MediatorLiveData<Resource<List<MyCryptocurrency>>>()
    private var liveDataMyCryptocurrencyResourceList: LiveData<Resource<List<MyCryptocurrency>>>
    private val liveDataMyCryptocurrencyList: LiveData<List<MyCryptocurrency>>
    var liveDataTotalHoldingsValueOnDateText: LiveData<String>
    val liveDataTotalHoldingsValueFiat24hText: LiveData<SpannableString>
    val liveDataTotalHoldingsValueCryptoText: LiveData<String>
    val liveDataTotalHoldingsValueFiatText: LiveData<String>
    var newSelectedFiatCurrencyCode: String? = null
    var isSwipeRefreshing: Boolean = false

    init {
        currentCryptoCurrencyCode = context.getString(R.string.default_crypto_code)
        currentCryptoCurrencySign = context.getString(R.string.default_crypto_sign)
        liveDataCurrentDateFormat = cryptocurrencyRepository.getCurrentDateFormatLiveData()
        liveDataCurrentFiatCurrencyCode = cryptocurrencyRepository.getCurrentFiatCurrencyCodeLiveData()
        liveDataCurrentFiatCurrencySign = cryptocurrencyRepository.getCurrentFiatCurrencySignLiveData()
        liveDataMyCryptocurrencyResourceList = cryptocurrencyRepository.getMyCryptocurrencyLiveDataResourceList(cryptocurrencyRepository.getCurrentFiatCurrencyCode())

        mediatorLiveDataMyCryptocurrencyResourceList.addSource(liveDataMyCryptocurrencyResourceList) {
            mediatorLiveDataMyCryptocurrencyResourceList.value = it
        }

        liveDataMyCryptocurrencyList = cryptocurrencyRepository.getMyCryptocurrencyLiveDataList()

        liveDataTotalHoldingsValueOnDateText = MediatorLiveData<String>().apply {
            addSource(liveDataMyCryptocurrencyList) {
                value = formatDate(getMyCryptocurrencyListLastFetchedDate(),
                        liveDataCurrentDateFormat.value,
                        cryptocurrencyRepository.getCurrentTimeFormat(),
                        stringsLocalization.getString(R.string.time_format_am),
                        stringsLocalization.getString(R.string.time_format_pm))
            }

            addSource(liveDataCurrentDateFormat) {
                value = formatDate(getMyCryptocurrencyListLastFetchedDate(),
                        liveDataCurrentDateFormat.value,
                        cryptocurrencyRepository.getCurrentTimeFormat(),
                        stringsLocalization.getString(R.string.time_format_am),
                        stringsLocalization.getString(R.string.time_format_pm))
            }

            addSource(cryptocurrencyRepository.getCurrentTimeFormatLiveData()) { timeFormat ->
                value = formatDate(getMyCryptocurrencyListLastFetchedDate(),
                        liveDataCurrentDateFormat.value,
                        timeFormat,
                        stringsLocalization.getString(R.string.time_format_am),
                        stringsLocalization.getString(R.string.time_format_pm))
            }
        }


        fun getMyCryptocurrencyListSumByDouble(sumFunc: (MyCryptocurrency) -> Double): Double {
            val code = liveDataCurrentFiatCurrencyCode.value ?: getCurrentFiatCurrencyCode()
            var sum = 0.0

            liveDataMyCryptocurrencyList.value?.forEach { myCryptocurrency ->
                if (myCryptocurrency.cryptoData.currencyFiat != code) {
                    sum = Double.NaN
                    return@forEach
                }
                sum += sumFunc(myCryptocurrency)
            }
            return sum
        }

        liveDataTotalHoldingsValueFiat24h = MediatorLiveData<Double>().apply {

            fun countTotalAmountFiatChange24h(): Double {
                return getMyCryptocurrencyListSumByDouble { cryptocurrency ->
                    cryptocurrency.amountFiatChange24h ?: 0.0
                }
            }

            addSource(liveDataMyCryptocurrencyList) {
                value = countTotalAmountFiatChange24h()
            }

            addSource(liveDataCurrentFiatCurrencyCode) {
                value = countTotalAmountFiatChange24h()
            }

        }

        liveDataTotalHoldingsValueFiat24hText = zip(liveDataCurrentFiatCurrencyCode, liveDataTotalHoldingsValueFiat24h)
        { currentFiatCurrencyCode, totalHoldingsValueFiat24h ->
            val currentFiatCurrencySign = cryptocurrencyRepository.getCurrentFiatCurrencySign(currentFiatCurrencyCode)
            getSpannableValueStyled(context, totalHoldingsValueFiat24h,
                    SpannableValueColorStyle.Background, ValueType.Fiat,
                    " $currentFiatCurrencySign ", " ", context.getString(R.string.string_no_number))
        }

        liveDataCurrentMyCryptocurrency = cryptocurrencyRepository.getSpecificCryptocurrencyLiveData(currentCryptoCurrencyCode)
        liveDataTotalHoldingsValueFiat = MediatorLiveData<Double>().apply {

            fun countTotalAmountFiat(): Double {
                return getMyCryptocurrencyListSumByDouble { cryptocurrency ->
                    cryptocurrency.amountFiat ?: 0.0
                }
            }

            addSource(liveDataMyCryptocurrencyList) {
                value = countTotalAmountFiat()
            }

            addSource(liveDataCurrentFiatCurrencyCode) {
                value = countTotalAmountFiat()
            }

        }

        liveDataTotalHoldingsValueFiatText = zip(liveDataCurrentFiatCurrencyCode, liveDataTotalHoldingsValueFiat)
        { currentFiatCurrencyCode, totalHoldingsValueFiat ->
            val currentFiatCurrencySign = cryptocurrencyRepository.getCurrentFiatCurrencySign(currentFiatCurrencyCode)
            String.format("$currentFiatCurrencySign ${if (totalHoldingsValueFiat.isNaN())
                context.getString(R.string.string_no_number) else
                roundValue(totalHoldingsValueFiat, ValueType.Fiat)}")
        }

        liveDataTotalHoldingsValueCrypto = zip(liveDataTotalHoldingsValueFiat, liveDataCurrentMyCryptocurrency)
        { totalHoldingsValueFiat, currentCryptocurrency ->
            totalHoldingsValueFiat / currentCryptocurrency.priceFiat
        }

        liveDataTotalHoldingsValueCryptoText = Transformations.switchMap(liveDataTotalHoldingsValueCrypto) { totalHoldingsValueCrypto ->
            MutableLiveData<String>().apply {
                value = String.format("$currentCryptoCurrencySign ${
                if (totalHoldingsValueCrypto.isNaN()) context.getString(R.string.string_no_number)
                else roundValue(totalHoldingsValueCrypto, ValueType.Crypto)}")
            }
        }

    }

    private fun getMyCryptocurrencyListLastFetchedDate(): Date? {
        val lastFetchedDate: Date? =
                liveDataMyCryptocurrencyList.value?.elementAtOrNull(0)?.cryptoData?.lastFetchedDate
                        ?: return null

        liveDataMyCryptocurrencyList.value?.forEach { myCryptocurrency ->
            if (myCryptocurrency.cryptoData.lastFetchedDate != lastFetchedDate) return null
        }

        return lastFetchedDate
    }

    private fun <A, B, C> zip(srcA: LiveData<A>, srcB: LiveData<B>, zipFunc: (A, B) -> C): LiveData<C> {

        return MediatorLiveData<C>().apply {
            var lastSrcA: A? = null
            var lastSrcB: B? = null

            fun update() {
                if (lastSrcA != null && lastSrcB != null)
                    value = zipFunc(lastSrcA!!, lastSrcB!!)
            }

            addSource(srcA) {
                lastSrcA = it
                update()
            }
            addSource(srcB) {
                lastSrcB = it
                update()
            }
        }

    }


    fun retry(newFiatCurrencyCode: String? = null) {
        newSelectedFiatCurrencyCode = newFiatCurrencyCode

        uiScope.launch {
            // Make a call to the server after some delay for better user experience.
            updateMyCryptocurrencyList(newFiatCurrencyCode, SERVER_CALL_DELAY_MILLISECONDS)
        }
    }

    fun refreshMyCryptocurrencyResourceList() {
        refreshMyCryptocurrencyResourceList(cryptocurrencyRepository.getMyCryptocurrencyLiveDataResourceList(cryptocurrencyRepository.getCurrentFiatCurrencyCode()))
    }

    private fun refreshMyCryptocurrencyResourceList(liveData: LiveData<Resource<List<MyCryptocurrency>>>) {
        mediatorLiveDataMyCryptocurrencyResourceList.removeSource(liveDataMyCryptocurrencyResourceList)
        liveDataMyCryptocurrencyResourceList = liveData
        mediatorLiveDataMyCryptocurrencyResourceList.addSource(liveDataMyCryptocurrencyResourceList)
        { mediatorLiveDataMyCryptocurrencyResourceList.value = it }
    }

    private suspend fun updateMyCryptocurrencyList(newFiatCurrencyCode: String? = null, callDelay: Long = 0) {

        val fiatCurrencyCode: String = newFiatCurrencyCode
                ?: cryptocurrencyRepository.getCurrentFiatCurrencyCode()

        isSwipeRefreshing = true

        val myCryptocurrencyIds = withContext(Dispatchers.IO) {
            cryptocurrencyRepository.getMyCryptocurrencyIds()
        }

        refreshMyCryptocurrencyResourceList(
                cryptocurrencyRepository.getMyCryptocurrencyLiveDataResourceList
                (fiatCurrencyCode, true, myCryptocurrencyIds, callDelay))
    }

    fun addCryptocurrency(myCryptocurrency: MyCryptocurrency) {
        // Launch a coroutine in uiScope.
        uiScope.launch {
            val refreshNeeded = withContext(Dispatchers.IO) {

                cryptocurrencyRepository.upsertMyCryptocurrency(myCryptocurrency)

                if (myCryptocurrency.cryptoData.currencyFiat != getCurrentFiatCurrencyCode())
                    return@withContext true

                val myCryptocurrencyList =
                        cryptocurrencyRepository.getMyCryptocurrencyList()

                if (myCryptocurrencyList.isNullOrEmpty()) return@withContext false

                myCryptocurrencyList.forEach {
                    if (myCryptocurrencyList.first().cryptoData.lastFetchedDate !=
                            it.cryptoData.lastFetchedDate)
                        return@withContext true
                }
                return@withContext false
            }

            if (refreshNeeded) {
                delay(500)
                updateMyCryptocurrencyList(callDelay = SERVER_CALL_DELAY_MILLISECONDS)
            } else {
                refreshMyCryptocurrencyResourceList()
            }
        }
    }

    fun deleteCryptocurrencyList(myCryptocurrencyList: List<MyCryptocurrency>) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                cryptocurrencyRepository.deleteMyCryptocurrencyList(myCryptocurrencyList)
            }
        }
    }


    fun restoreCryptocurrencyList(myCryptocurrencyList: List<MyCryptocurrency>) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                cryptocurrencyRepository.insertMyCryptocurrencyList(myCryptocurrencyList)
            }
        }
    }


    fun getCurrentFiatCurrencyCode(): String {
        return cryptocurrencyRepository.getCurrentFiatCurrencyCode()
    }

    fun getSelectedFiatCurrencyCodeFromRep(): String {
        return cryptocurrencyRepository.selectedFiatCurrencyCode
    }

    fun setSelectedFiatCurrencyCodeFromRep(code: String) {
        cryptocurrencyRepository.selectedFiatCurrencyCode = code
    }

    fun setNewCurrentFiatCurrencyCode(value: String) {
        cryptocurrencyRepository.setNewCurrentFiatCurrencyCode(value)
    }

    fun checkIfNewFiatCurrencyCodeSameToMyCryptocurrency(newFiatCurrencyCode: String): Boolean {
        liveDataMyCryptocurrencyList.value?.forEach { myCryptocurrency ->
            if (myCryptocurrency.cryptoData.currencyFiat != newFiatCurrencyCode) {
                return false
            }
        }
        return true
    }
}