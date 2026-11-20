package com.payent.delivery.core.common

import com.payent.delivery.BuildConfig

object AppConstants {
    const val DEFAULT_BASE_URL: String = BuildConfig.BASE_URL
    const val NETWORK_TIMEOUT_SECONDS: Long = 30L
    const val PREFS_SESSION_NAME: String = "payent_delivery_session"

    // Configuration Placeholder Info
    const val DEV_BASE_URL_EXAMPLE: String = "http://10.0.2.2:8000/api/v1/"
    const val PROD_BASE_URL_EXAMPLE: String = "https://api.payent.com/v1/"
}
