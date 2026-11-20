package com.payent.delivery.domain.repository

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.EarningsSummary

interface EarningsRepository {
    suspend fun getEarningsSummary(): NetworkResult<EarningsSummary>
}
