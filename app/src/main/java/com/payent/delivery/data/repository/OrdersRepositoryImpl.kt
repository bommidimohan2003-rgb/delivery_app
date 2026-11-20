package com.payent.delivery.data.repository

import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.network.safeApiCall
import com.payent.delivery.data.remote.DeliveryApiService
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.repository.OrdersRepository

class OrdersRepositoryImpl(
    private val deliveryApiService: DeliveryApiService,
    private val dispatchers: DispatcherProvider
) : OrdersRepository {

    override suspend fun getAvailableOrders(): NetworkResult<List<Order>> {
        return safeApiCall(dispatchers.io) {
            deliveryApiService.getAvailableOrders()
        }
    }

    override suspend fun getActiveOrders(): NetworkResult<List<Order>> {
        return safeApiCall(dispatchers.io) {
            deliveryApiService.getActiveOrders()
        }
    }

    override suspend fun getOrderHistory(): NetworkResult<List<Order>> {
        return safeApiCall(dispatchers.io) {
            deliveryApiService.getOrderHistory()
        }
    }

    override suspend fun acceptOrder(orderId: String): NetworkResult<Order> {
        return safeApiCall(dispatchers.io) {
            deliveryApiService.acceptOrder(orderId)
        }
    }

    override suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean> {
        return safeApiCall(dispatchers.io) {
            deliveryApiService.updateAvailabilityStatus(isOnline)
            true
        }
    }
}
