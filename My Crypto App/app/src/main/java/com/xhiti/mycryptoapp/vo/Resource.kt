package com.baruckis.kriptofolio.vo

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> successDb(data: T?): Resource<T> {
            return Resource(Status.SUCCESS_DB, data, null)
        }

        fun <T> successNetwork(data: T?): Resource<T> {
            return Resource(Status.SUCCESS_NETWORK, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}