package com.payent.delivery.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): NetworkResult<T> {
    return withContext(dispatcher) {
        try {
            NetworkResult.Success(apiCall())
        } catch (e: HttpException) {
            val code = e.code()
            val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
            NetworkResult.ApiError(code, errorMsg)
        } catch (e: IOException) {
            NetworkResult.NetworkError(e)
        } catch (e: Throwable) {
            NetworkResult.UnknownError(e)
        }
    }
}
