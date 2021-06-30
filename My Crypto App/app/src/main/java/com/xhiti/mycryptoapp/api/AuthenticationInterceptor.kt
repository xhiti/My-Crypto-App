package com.baruckis.kriptofolio.api

import com.baruckis.kriptofolio.utilities.API_SERVICE_AUTHENTICATION_KEY
import com.baruckis.kriptofolio.utilities.API_SERVICE_AUTHENTICATION_NAME
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val newRequest = chain.request().newBuilder()
                .addHeader(API_SERVICE_AUTHENTICATION_NAME, API_SERVICE_AUTHENTICATION_KEY)
                .build()

        return chain.proceed(newRequest)
    }
}