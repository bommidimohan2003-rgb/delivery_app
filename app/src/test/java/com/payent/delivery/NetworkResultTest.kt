package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.network.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkResultTest {

    @Test
    fun safeApiCall_returnsSuccess_whenApiCallSucceeds() = runTest {
        val result = safeApiCall(Dispatchers.Unconfined) {
            "Success response"
        }

        assertTrue(result is NetworkResult.Success)
        assertEquals("Success response", (result as NetworkResult.Success).data)
    }

    @Test
    fun safeApiCall_returnsNetworkError_whenIOExceptionThrown() = runTest {
        val result = safeApiCall(Dispatchers.Unconfined) {
            throw IOException("Network offline")
        }

        assertTrue(result is NetworkResult.NetworkError)
    }
}
