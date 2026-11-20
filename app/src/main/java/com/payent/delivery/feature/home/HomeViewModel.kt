package com.payent.delivery.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.domain.repository.OrdersRepository
import com.payent.delivery.feature.orders.OrdersViewModel
import com.payent.delivery.feature.orders.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val ordersRepository: OrdersRepository,
    private val sessionManager: SessionManager? = null,
    private val ordersViewModel: OrdersViewModel? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPartnerData()
        if (sessionManager != null) {
            viewModelScope.launch {
                sessionManager.isOnlineStatusFlow.collect { isOnline ->
                    _uiState.update { it.copy(isOnline = isOnline) }
                    ordersViewModel?.updateOnlineStatus(isOnline)
                }
            }
        }
        if (ordersViewModel != null) {
            viewModelScope.launch {
                ordersViewModel.uiState.collect { ordersState ->
                    _uiState.update { homeState ->
                        homeState.copy(
                            activeOrdersCount = ordersState.activeOrders.count { it.status.isActive },
                            activeOrder = ordersState.activeOrders.firstOrNull { it.status.isActive },
                            availableOrdersPreview = ordersState.availableOrders
                        )
                    }
                }
            }
        }
    }

    fun loadPartnerData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentAvailable = ordersViewModel?.uiState?.value?.availableOrders ?: OrdersViewModel.sampleDemoOrders
            val currentActiveCount = ordersViewModel?.uiState?.value?.activeOrders?.count { it.status.isActive } ?: 0
            val currentActive = ordersViewModel?.uiState?.value?.activeOrders?.firstOrNull { it.status.isActive }

            when (val result = authRepository.getPartnerProfile()) {
                is NetworkResult.Success -> {
                    val isConnected = result.data.id != "demo_partner"
                    _uiState.update {
                        it.copy(
                            partner = result.data,
                            isOnline = if (sessionManager == null) result.data.isOnline else it.isOnline,
                            activeOrdersCount = currentActiveCount,
                            activeOrder = currentActive,
                            availableOrdersPreview = currentAvailable,
                            isLoading = false,
                            isBackendConnected = isConnected
                        )
                    }
                }
                else -> {
                    val fallbackPartner = DeliveryPartner(
                        id = "demo_partner",
                        name = "Demo Delivery Partner",
                        phoneNumber = "+1 000 000 0000",
                        email = "demo.delivery@example.com"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isBackendConnected = false,
                            partner = fallbackPartner,
                            activeOrdersCount = currentActiveCount,
                            activeOrder = currentActive,
                            availableOrdersPreview = currentAvailable
                        )
                    }
                }
            }
        }
    }

    fun toggleOnlineStatus(online: Boolean) {
        val currentState = _uiState.value
        if (currentState.isUpdatingStatus || currentState.isOnline == online) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
            when (val result = ordersRepository.updateAvailabilityStatus(online)) {
                is NetworkResult.Success -> {
                    sessionManager?.saveOnlineStatus(online)
                    ordersViewModel?.updateOnlineStatus(online)
                    _uiState.update {
                        it.copy(
                            isOnline = online,
                            isUpdatingStatus = false,
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.ApiError -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = result.message
                        )
                    }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    if (!_uiState.value.isBackendConnected) {
                        sessionManager?.saveOnlineStatus(online)
                        ordersViewModel?.updateOnlineStatus(online)
                        _uiState.update {
                            it.copy(
                                isOnline = online,
                                isUpdatingStatus = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        val errorMsg = when (result) {
                            is NetworkResult.NetworkError -> result.exception.message
                            is NetworkResult.UnknownError -> result.exception.message
                            else -> null
                        } ?: "Error updating availability status"
                        _uiState.update {
                            it.copy(
                                isUpdatingStatus = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
                }
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val ordersRepository: OrdersRepository,
        private val sessionManager: SessionManager? = null,
        private val ordersViewModel: OrdersViewModel? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(authRepository, ordersRepository, sessionManager, ordersViewModel) as T
        }
    }
}
