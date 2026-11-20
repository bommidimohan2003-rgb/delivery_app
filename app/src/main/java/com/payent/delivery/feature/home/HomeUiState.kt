package com.payent.delivery.feature.home

import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.Order

data class HomeUiState(
    val partner: DeliveryPartner? = null,
    val isOnline: Boolean = false,
    val activeOrdersCount: Int = 0,
    val activeOrder: Order? = null,
    val availableOrdersPreview: List<Order> = emptyList(),
    val todayEarnings: Double = 0.0,
    val completedDeliveriesCount: Int = 0,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null,
    val isBackendConnected: Boolean = false
)
