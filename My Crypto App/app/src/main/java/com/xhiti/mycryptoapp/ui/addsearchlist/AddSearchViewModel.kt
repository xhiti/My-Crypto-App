package com.baruckis.kriptofolio.ui.addsearchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.baruckis.kriptofolio.db.Cryptocurrency
import com.baruckis.kriptofolio.db.MyCryptocurrency
import com.baruckis.kriptofolio.repository.CryptocurrencyRepository
import com.baruckis.kriptofolio.ui.common.BaseViewModel
import com.baruckis.kriptofolio.utilities.SERVER_CALL_DELAY_MILLISECONDS
import com.baruckis.kriptofolio.utilities.TimeFormat
import com.baruckis.kriptofolio.vo.Resource
import javax.inject.Inject

class AddSearchViewModel @Inject constructor(var cryptocurrencyRepository: CryptocurrencyRepository) : BaseViewModel() {

    val mediatorLiveDataCryptocurrencyResourceList = MediatorLiveData<Resource<List<Cryptocurrency>>>()
    private var liveDataCryptocurrencyResourceList: LiveData<Resource<List<Cryptocurrency>>> =
            cryptocurrencyRepository.getAllCryptocurrencyLiveDataResourceList(cryptocurrencyRepository.getCurrentFiatCurrencyCode())

    var selectedCryptocurrency: MyCryptocurrency? = null
    var isSwipeRefreshing: Boolean = false
    var lastUpdatedOnDate: String = ""
    var isSearchMenuItemEnabled: Boolean = true

    init {
        mediatorLiveDataCryptocurrencyResourceList.addSource(liveDataCryptocurrencyResourceList) {
            mediatorLiveDataCryptocurrencyResourceList.value = it
        }
    }

    fun retry() {
        refreshCryptocurrencyResourceList(SERVER_CALL_DELAY_MILLISECONDS)
    }

    private fun refreshCryptocurrencyResourceList(callDelay: Long = 0) {
        mediatorLiveDataCryptocurrencyResourceList.removeSource(liveDataCryptocurrencyResourceList)
        liveDataCryptocurrencyResourceList = cryptocurrencyRepository.getAllCryptocurrencyLiveDataResourceList(
                cryptocurrencyRepository.getCurrentFiatCurrencyCode(),
                true, callDelay)
        mediatorLiveDataCryptocurrencyResourceList.addSource(liveDataCryptocurrencyResourceList) {
            mediatorLiveDataCryptocurrencyResourceList.value = it
        }
    }

    fun search(searchText: String): LiveData<List<Cryptocurrency>> {
        return cryptocurrencyRepository.getCryptocurrencyLiveDataListBySearch(searchText)
    }

    fun getCurrentDateFormat(): String {
        return cryptocurrencyRepository.getCurrentDateFormat()
    }

    fun getCurrentTimeFormat(): TimeFormat {
        return cryptocurrencyRepository.getCurrentTimeFormat()
    }

}