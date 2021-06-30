package com.baruckis.kriptofolio.repository

import android.os.Handler
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.baruckis.kriptofolio.api.ApiEmptyResponse
import com.baruckis.kriptofolio.api.ApiErrorResponse
import com.baruckis.kriptofolio.api.ApiResponse
import com.baruckis.kriptofolio.api.ApiSuccessResponse
import com.baruckis.kriptofolio.vo.Resource

abstract class NetworkBoundResource<ResultType, RequestType>
@MainThread constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        @Suppress("LeakingThis")
        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) { newData ->
                    setValue(Resource.successDb(newData))
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createCall()
        result.addSource(dbSource) { newData ->
            setValue(Resource.loading(newData))
        }

        fun fetch() {
            result.addSource(apiResponse) { response ->
                result.removeSource(apiResponse)
                result.removeSource(dbSource)
                when (response) {
                    is ApiSuccessResponse -> {
                        appExecutors.diskIO().execute {
                            saveCallResult(processResponse(response))
                            appExecutors.mainThread().execute {
                                result.addSource(loadFromDb()) { newData ->
                                    setValue(Resource.successNetwork(newData))
                                }
                            }
                        }
                    }
                    is ApiEmptyResponse -> {
                        appExecutors.mainThread().execute {
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Resource.successDb(newData))
                            }
                        }
                    }
                    is ApiErrorResponse -> {
                        onFetchFailed()
                        result.addSource(dbSource) { newData ->
                            setValue(Resource.error(response.errorMessage, newData))
                        }
                    }
                }
            }
        }

        val delay = fetchDelayMillis()
        if (delay > 0) {
            Handler().postDelayed({ fetch() }, delay)
        } else fetch()

    }

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected open fun fetchDelayMillis(): Long = 0

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}
