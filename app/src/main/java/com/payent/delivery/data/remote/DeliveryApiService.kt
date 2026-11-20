package com.payent.delivery.data.remote

import com.payent.delivery.domain.model.Order
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DeliveryApiService {
    @GET("orders/available")
    suspend fun getAvailableOrders(): List<Order>

    @GET("orders/active")
    suspend fun getActiveOrders(): List<Order>

    @GET("orders/history")
    suspend fun getOrderHistory(): List<Order>

    @POST("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") orderId: String): Order

    @POST("partner/status")
    suspend fun updateAvailabilityStatus(@Query("isOnline") isOnline: Boolean)
}
