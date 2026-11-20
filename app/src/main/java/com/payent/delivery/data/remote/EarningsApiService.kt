package com.payent.delivery.data.remote

import com.payent.delivery.domain.model.EarningsSummary
import retrofit2.http.GET

interface EarningsApiService {
    @GET("earnings/summary")
    suspend fun getEarningsSummary(): EarningsSummary
}
