package com.payent.delivery.domain.repository

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.Order

interface OrdersRepository {
    suspend fun getAvailableOrders(): NetworkResult<List<Order>>
    suspend fun getActiveOrders(): NetworkResult<List<Order>>
    suspend fun getOrderHistory(): NetworkResult<List<Order>>
    suspend fun acceptOrder(orderId: String): NetworkResult<Order>
    suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean>
}
