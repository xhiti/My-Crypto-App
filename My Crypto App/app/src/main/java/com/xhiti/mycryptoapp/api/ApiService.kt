package com.baruckis.kriptofolio.api

import androidx.lifecycle.LiveData
import com.baruckis.kriptofolio.utilities.API_SERVICE_RESULTS_LIMIT
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("v1/cryptocurrency/listings/latest")
    fun getAllCryptocurrencies(@Query("convert") currency: String,
                               @Query("limit") size: Int = API_SERVICE_RESULTS_LIMIT):
            LiveData<ApiResponse<CoinMarketCap<List<CryptocurrencyLatest>>>>

    @GET("v1/cryptocurrency/quotes/latest")
    fun getCryptocurrenciesById(@Query("convert") currency: String,
                                @Query("id") id: String):
            LiveData<ApiResponse<CoinMarketCap<HashMap<String, CryptocurrencyLatest>>>>
}