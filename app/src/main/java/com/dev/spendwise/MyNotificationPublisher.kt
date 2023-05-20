package com.dev.spendwise

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


class MyNotificationPublisher : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification? = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val addExpenseIntent = Intent(context, MainActivity::class.java)
        addExpenseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, addExpenseIntent, PendingIntent.FLAG_IMMUTABLE)
        if (notification != null) {
            notification.contentIntent = pendingIntent
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                "10001",
                "NOTIFICATION_CHANNEL_NAME",
                importance
            )
            assert(notificationManager != null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val id: Int = intent.getIntExtra(NOTIFICATION_ID, 0)
        assert(notificationManager != null)
        notificationManager.notify(id, notification)
    }

    companion object {
        @JvmField
        var NOTIFICATION_ID = "notification-id"
        @JvmField
        var NOTIFICATION = "notification"
    }
}