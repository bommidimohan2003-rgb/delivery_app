package com.payent.delivery.core.alert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.payent.delivery.MainActivity
import com.payent.delivery.R
import com.payent.delivery.domain.model.Order
import com.payent.delivery.feature.orders.getOrderDisplayInfo

interface OrderAlertNotifier {
    fun createNotificationChannel()
    fun postOrderAlertNotification(event: OrderAssignmentEvent, order: Order)
    fun cancelOrderAlertNotification(orderId: String)
    fun cancelAllNotifications()
}

/**
 * Handles creation of the high-importance Notification Channel and construction
 * of Android heads-up order assignment notifications.
 */
class OrderAlertNotificationHelper(
    private val context: Context
) : OrderAlertNotifier {
    companion object {
        const val CHANNEL_ID = "payent_order_assignments_channel"
        const val CHANNEL_NAME = "New Order Assignments"
        const val CHANNEL_DESCRIPTION = "High-importance alerts for newly assigned delivery orders"

        const val EXTRA_ORDER_ID = "com.payent.delivery.extra.ORDER_ID"
        const val EXTRA_EVENT_ID = "com.payent.delivery.extra.EVENT_ID"
        const val EXTRA_ACTION = "com.payent.delivery.extra.ACTION"

        const val ACTION_VIEW_ASSIGNMENT = "com.payent.delivery.action.VIEW_ASSIGNMENT"
        const val ACTION_ACCEPT_ASSIGNMENT = "com.payent.delivery.action.ACCEPT_ASSIGNMENT"
        const val ACTION_DISMISS_ASSIGNMENT = "com.payent.delivery.action.DISMISS_ASSIGNMENT"

        // Base Notification ID offset
        private const val NOTIFICATION_ID_BASE = 10000
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Creates or updates the notification channel with high importance, custom audio, and repeating vibration.
     */
    override fun createNotificationChannel() {
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.order_alert}")
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
            lightColor = Color.GREEN
            enableVibration(true)
            vibrationPattern = AlertSoundVibrationPlayer.REPEATING_VIBRATION_PATTERN
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
        }

        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Posts a high-priority heads-up notification with action intents.
     */
    override fun postOrderAlertNotification(event: OrderAssignmentEvent, order: Order) {
        val displayInfo = getOrderDisplayInfo(order)
        val notificationId = getNotificationId(order.id)

        // 1. Content Intent: View Order in MainActivity
        val viewIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_VIEW_ASSIGNMENT
            putExtra(EXTRA_ORDER_ID, order.id)
            putExtra(EXTRA_EVENT_ID, event.eventId)
            putExtra(EXTRA_ACTION, "VIEW")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val viewPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Action Intent: Direct Accept Order
        val acceptIntent = Intent(context, OrderAlertReceiver::class.java).apply {
            action = ACTION_ACCEPT_ASSIGNMENT
            putExtra(EXTRA_ORDER_ID, order.id)
            putExtra(EXTRA_EVENT_ID, event.eventId)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Action Intent: Dismiss Alert
        val dismissIntent = Intent(context, OrderAlertReceiver::class.java).apply {
            action = ACTION_DISMISS_ASSIGNMENT
            putExtra(EXTRA_ORDER_ID, order.id)
            putExtra(EXTRA_EVENT_ID, event.eventId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.order_alert}")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.partner_delivery_bike)
            .setContentTitle("New Order Assigned: ${order.orderNumber}")
            .setContentText("${displayInfo.title} • ₹${order.payoutAmount.toInt()} • Pickup: ${order.vendorAddress}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Order ID: ${order.orderNumber}\n" +
                        "Item: ${displayInfo.title} (${displayInfo.category})\n" +
                        "Pickup: ${order.vendorAddress} (${displayInfo.pickupDistance})\n" +
                        "Delivery: ${order.deliveryAddress} (${displayInfo.deliveryDistance})\n" +
                        "Payout: ₹${order.payoutAmount.toInt()}\n" +
                        "Pickup Window: ${displayInfo.schedule}"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(AlertSoundVibrationPlayer.REPEATING_VIBRATION_PATTERN)
            .setContentIntent(viewPendingIntent)
            .setDeleteIntent(dismissPendingIntent)
            .addAction(0, "View Order", viewPendingIntent)
            .addAction(0, "Accept Order", acceptPendingIntent)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)

        val notification = builder.build()
        // FLAG_INSISTENT repeats sound and vibration in background until acknowledged or cancelled
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        notificationManager.notify(notificationId, notification)
    }

    /**
     * Cancels an active order assignment notification.
     */
    override fun cancelOrderAlertNotification(orderId: String) {
        val notificationId = getNotificationId(orderId)
        notificationManager.cancel(notificationId)
    }

    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun getNotificationId(orderId: String): Int {
        return NOTIFICATION_ID_BASE + (orderId.hashCode() and 0x7FFFFFFF % 10000)
    }
}
