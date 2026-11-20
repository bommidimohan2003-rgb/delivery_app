package com.payent.delivery

import android.app.Application
import com.payent.delivery.core.AppContainer

class PayentApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
