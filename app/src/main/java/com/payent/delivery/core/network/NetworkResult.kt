package com.payent.delivery.core.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class ApiError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkError(val exception: Throwable) : NetworkResult<Nothing>()
    data class UnknownError(val exception: Throwable) : NetworkResult<Nothing>()
}
