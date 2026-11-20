package com.payent.delivery.data.repository

import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.network.safeApiCall
import com.payent.delivery.data.remote.EarningsApiService
import com.payent.delivery.domain.model.EarningsSummary
import com.payent.delivery.domain.repository.EarningsRepository

class EarningsRepositoryImpl(
    private val earningsApiService: EarningsApiService,
    private val dispatchers: DispatcherProvider
) : EarningsRepository {

    override suspend fun getEarningsSummary(): NetworkResult<EarningsSummary> {
        return safeApiCall(dispatchers.io) {
            earningsApiService.getEarningsSummary()
        }
    }
}
