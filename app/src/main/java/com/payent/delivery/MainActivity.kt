package com.payent.delivery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.payent.delivery.core.AppContainer
import com.payent.delivery.core.alert.OrderAlertManager

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = (application as PayentApplication).appContainer

        requestNotificationPermission()
        handleIntent(intent)

        setContent {
            PayentApp(appContainer = appContainer)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        appContainer.orderAlertManager.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        appContainer.orderAlertManager.onAppBackgrounded()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == OrderAlertManager.ACTION_VIEW_ASSIGNMENT) {
            val orderId = intent.getStringExtra(OrderAlertManager.EXTRA_ORDER_ID)
            if (!orderId.isNullOrBlank()) {
                appContainer.orderAlertManager.acknowledgeAlert(orderId)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
