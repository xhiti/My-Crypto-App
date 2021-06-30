package com.baruckis.kriptofolio.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response

@Suppress("unused")
sealed class ApiResponse<CoinMarketCapType> {
    companion object {
        fun <CoinMarketCapType> create(error: Throwable): ApiErrorResponse<CoinMarketCapType> {
            return ApiErrorResponse(error.message ?: "Unknown error.")
        }

        fun <CoinMarketCapType> create(response: Response<CoinMarketCapType>): ApiResponse<CoinMarketCapType> {
            return if (response.isSuccessful) {
                val body = response.body()
                if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(body = body)
                }
            } else {

                var errorMsg: String? = null

                if (response.errorBody()?.contentType()?.subtype.equals("json")) {

                    errorMsg = try {

                        val gson = Gson()
                        val type = object : TypeToken<CoinMarketCap<CoinMarketCapType>>() {}.type
                        val errorResponse: CoinMarketCap<CoinMarketCapType> = gson.fromJson(response.errorBody()!!.charStream(), type)

                        errorResponse.status?.errorMessage ?: errorResponse.message

                    } catch (e: Exception) {
                        ""
                    }

                }

                if (errorMsg.isNullOrEmpty()) {
                    val msg = response.errorBody()?.string()
                    errorMsg = if (msg.isNullOrEmpty()) {
                        response.message()
                    } else {
                        msg
                    }
                }

                ApiErrorResponse(errorMsg ?: "Unknown error.")
            }
        }
    }
}


class ApiEmptyResponse<CoinMarketCapType> : ApiResponse<CoinMarketCapType>()

data class ApiSuccessResponse<CoinMarketCapType>(val body: CoinMarketCapType) : ApiResponse<CoinMarketCapType>()

data class ApiErrorResponse<CoinMarketCapType>(val errorMessage: String) : ApiResponse<CoinMarketCapType>()