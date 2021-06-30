package com.baruckis.kriptofolio.api

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Response

@RunWith(JUnit4::class)
class ApiResponseTest {

    @Test
    fun exception() {
        val exception = Exception("foo")
        val (errorMessage) = ApiResponse.create<String>(exception)
        MatcherAssert.assertThat<String>(errorMessage, CoreMatchers.`is`("foo"))
    }

    @Test
    fun success() {
        val apiResponse: ApiSuccessResponse<String> = ApiResponse
                .create<String>(Response.success("foo")) as ApiSuccessResponse<String>
        MatcherAssert.assertThat<String>(apiResponse.body, CoreMatchers.`is`("foo"))
    }

    @Test
    fun error() {
        val errorResponse = Response.error<String>(
                400,
                ResponseBody.create("application/txt".toMediaTypeOrNull(), "blah")
        )
        val (errorMessage) = ApiResponse.create<String>(errorResponse) as ApiErrorResponse<String>
        MatcherAssert.assertThat<String>(errorMessage, CoreMatchers.`is`("blah"))
    }
}