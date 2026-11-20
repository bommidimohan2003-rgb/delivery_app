package com.payent.delivery.core

import android.content.Context
import com.payent.delivery.core.alert.OrderAlertManager
import com.payent.delivery.core.common.AppConstants
import com.payent.delivery.core.common.DefaultDispatcherProvider
import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.ApiClientFactory
import com.payent.delivery.core.security.EncryptedSessionManager
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.data.remote.AuthApiService
import com.payent.delivery.data.remote.DeliveryApiService
import com.payent.delivery.data.remote.EarningsApiService
import com.payent.delivery.data.repository.AuthRepositoryImpl
import com.payent.delivery.data.repository.EarningsRepositoryImpl
import com.payent.delivery.data.repository.OrdersRepositoryImpl
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.domain.repository.EarningsRepository
import com.payent.delivery.domain.repository.OrdersRepository

class AppContainer(private val context: Context) {

    val dispatchers: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    val sessionManager: SessionManager by lazy {
        EncryptedSessionManager(context)
    }

    private val okHttpClient by lazy {
        ApiClientFactory.createOkHttpClient {
            sessionManager.getAuthTokenSync()
        }
    }

    private val retrofit by lazy {
        ApiClientFactory.createRetrofit(
            baseUrl = AppConstants.DEFAULT_BASE_URL,
            okHttpClient = okHttpClient
        )
    }

    val authApiService: AuthApiService by lazy {
        ApiClientFactory.createApiService(retrofit)
    }

    val deliveryApiService: DeliveryApiService by lazy {
        ApiClientFactory.createApiService(retrofit)
    }

    val earningsApiService: EarningsApiService by lazy {
        ApiClientFactory.createApiService(retrofit)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authApiService = authApiService,
            sessionManager = sessionManager,
            dispatchers = dispatchers
        )
    }

    val ordersRepository: OrdersRepository by lazy {
        OrdersRepositoryImpl(
            deliveryApiService = deliveryApiService,
            dispatchers = dispatchers
        )
    }

    val earningsRepository: EarningsRepository by lazy {
        EarningsRepositoryImpl(
            earningsApiService = earningsApiService,
            dispatchers = dispatchers
        )
    }

    val orderAlertManager: OrderAlertManager by lazy {
        OrderAlertManager(
            context = context,
            sessionManager = sessionManager,
            ordersRepository = ordersRepository,
            dispatchers = dispatchers
        )
    }
}
